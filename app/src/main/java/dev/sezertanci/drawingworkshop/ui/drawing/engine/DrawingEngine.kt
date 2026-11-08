package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Size
import dagger.hilt.android.scopes.ViewModelScoped
import dev.sezertanci.drawingworkshop.di.DefaultDispatcher
import dev.sezertanci.drawingworkshop.domain.fill.FloodFiller
import dev.sezertanci.drawingworkshop.domain.history.UndoRedoHistory
import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.Palette
import dev.sezertanci.drawingworkshop.domain.model.StrokeSettings
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class HistoryState(val canUndo: Boolean, val canRedo: Boolean)

/**
 * Tuvalin çizim motoru: çizgileri, boya kovasını ve geri al / yinele geçmişini yönetir.
 *
 * Katmanlar alttan üste: arka plan rengi, boya katmanı, o an çizilen çizgi ve varsa boyama sayfasının
 * çizgileri. Çizgiler ayrı tutulduğu için fırça onları örtmez, silgi silmez; boya kovası onları duvar sayar.
 *
 * Tüm metotlar ana iş parçacığından çağrılır. Yalnızca boya kovasının piksel hesabı arka planda
 * yapılır; bu sırada yeni çizgi ve geri alma kabul edilmez, böylece bitmap tutarlı kalır.
 */
@ViewModelScoped
class DrawingEngine @Inject constructor(
    private val strokeFactory: StrokeFactory,
    private val floodFiller: FloodFiller,
    @param:DefaultDispatcher private val fillDispatcher: CoroutineDispatcher,
) {
    private val surface = DrawingSurface()
    private val history = UndoRedoHistory<RegionSnapshot>(UNDO_CAPACITY, RegionSnapshot::release)
    private val _historyState = MutableStateFlow(HistoryState(canUndo = false, canRedo = false))
    private val _contentVersion = MutableStateFlow(0)
    private val _canvasSize = MutableStateFlow<Size?>(null)
    private var activeStroke: ActiveStroke? = null
    private var lineArt: LineArt? = null
    private var isFilling = false
    private var onChanged: (() -> Unit)? = null

    val historyState: StateFlow<HistoryState> = _historyState.asStateFlow()

    /** Tuvalin piksel boyutu; görünüm ölçülene kadar `null`. Çizgi katmanı bu boyutta hazırlanır. */
    val canvasSize: StateFlow<Size?> = _canvasSize.asStateFlow()

    /** Resim her değiştiğinde artar (çizgi, doldurma, geri al, arka plan). Otomatik kayıt bunu izler. */
    val contentVersion: StateFlow<Int> = _contentVersion.asStateFlow()

    var settings: StrokeSettings = StrokeSettings(ToolType.BRUSH, BrushSize.MEDIUM, Palette.RED)

    var backgroundColor: Int = Palette.WHITE
        set(value) {
            if (field == value) return
            field = value
            markContentChanged()
            notifyChanged()
        }

    /** Görünüm, yeniden çizilmesi gerektiğinde haber alır. Motor görünümden uzun yaşadığı için `null` ile bırakılır. */
    fun setOnChangedListener(listener: (() -> Unit)?) {
        onChanged = listener
    }

    fun resize(width: Int, height: Int) {
        cancelStroke()
        surface.resize(width, height)
        if (surface.width > 0) _canvasSize.value = Size(surface.width, surface.height)
        notifyChanged()
    }

    /**
     * Kayıtlı resmin boya katmanını yükler. Yalnızca açılışta, çizim başlamadan çağrılır; geçmiş
     * temizlenir. [layer] motora devredilir.
     */
    fun load(layer: Bitmap) {
        cancelStroke()
        history.clear()
        surface.load(layer)
        publishHistory()
        notifyChanged()
    }

    /** Boyama sayfasının çizgilerini yerleştirir; önceki çizgiler serbest bırakılır. [art] motora devredilir. */
    fun setLineArt(art: LineArt) {
        lineArt?.release()
        lineArt = art
        notifyChanged()
    }

    /** Boya katmanının kopyası; serbest bırakmak çağırana aittir. */
    fun copyLayer(): Bitmap? = surface.copy()

    fun draw(canvas: Canvas) {
        canvas.drawColor(backgroundColor)
        surface.drawTo(canvas)
        activeStroke?.drawPreview(canvas)
        currentLineArt()?.let { canvas.drawBitmap(it.bitmap, 0f, 0f, null) }
    }

    /** [zoom] büyüdükçe çizgi resim üzerinde incelir; böylece ekranda aynı kalınlıkta görünür. */
    fun startStroke(x: Float, y: Float, zoom: Float) {
        if (isFilling) return
        activeStroke = strokeFactory.create(settings, backgroundColor, zoom)?.also { it.begin(x, y) }
        notifyChanged()
    }

    fun extendStroke(x: Float, y: Float) {
        activeStroke?.extend(x, y)
    }

    /** Görünüm bir dokunma olayındaki tüm noktaları ekledikten sonra bir kez çağırır. */
    fun invalidate() = notifyChanged()

    fun endStroke() {
        val stroke = activeStroke ?: return
        activeStroke = null
        stroke.end()
        surface.snapshot(stroke.dirtyRegion())?.let { before ->
            history.record(before)
            surface.paint(stroke::commit)
            markContentChanged()
        }
        publishHistory()
        notifyChanged()
    }

    fun cancelStroke() {
        if (activeStroke == null) return
        activeStroke = null
        notifyChanged()
    }

    suspend fun fill(x: Float, y: Float, color: Int) {
        if (isFilling) return
        val width = surface.width
        val height = surface.height
        isFilling = true
        try {
            val pixels = surface.readPixels()
            val walls = currentLineArt()?.walls
            val bounds = withContext(fillDispatcher) {
                floodFiller.fill(pixels, width, height, x.toInt(), y.toInt(), color, walls)
            } ?: return
            if (surface.width != width || surface.height != height) return
            surface.snapshot(bounds.toRect())?.let(history::record)
            surface.writePixels(pixels, bounds)
            markContentChanged()
            publishHistory()
            notifyChanged()
        } finally {
            isFilling = false
        }
    }

    fun undo() = applyHistory { history.undo(surface::restore) }

    fun redo() = applyHistory { history.redo(surface::restore) }

    fun release() {
        onChanged = null
        history.clear()
        surface.release()
        lineArt?.release()
        lineArt = null
    }

    private fun applyHistory(action: () -> Boolean) {
        if (isFilling || activeStroke != null) return
        if (action()) {
            markContentChanged()
            publishHistory()
            notifyChanged()
        }
    }

    /** Tuval yeniden boyutlanıp çizgiler henüz yenilenmediyse eski çizgiler kullanılmaz. */
    private fun currentLineArt(): LineArt? = lineArt?.takeIf { it.matches(surface.width, surface.height) }

    private fun publishHistory() {
        _historyState.value = HistoryState(history.canUndo, history.canRedo)
    }

    private fun markContentChanged() {
        _contentVersion.value += 1
    }

    private fun notifyChanged() {
        onChanged?.invoke()
    }

    private companion object {
        const val UNDO_CAPACITY = 5
    }
}
