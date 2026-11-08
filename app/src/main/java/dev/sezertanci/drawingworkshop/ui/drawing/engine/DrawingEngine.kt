package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Size
import dagger.hilt.android.scopes.ViewModelScoped
import dev.sezertanci.drawingworkshop.di.DefaultDispatcher
import dev.sezertanci.drawingworkshop.domain.fill.FloodFiller
import dev.sezertanci.drawingworkshop.domain.history.UndoRedoHistory
import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.Palette
import dev.sezertanci.drawingworkshop.domain.model.StrokeSettings
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.domain.sticker.StickerEditor
import dev.sezertanci.drawingworkshop.domain.sticker.StickerTouchResult
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
 * Katmanlar alttan üste: arka plan rengi, boya katmanı, o an çizilen çizgi, düzenlenen çıkartma ve varsa
 * boyama sayfasının çizgileri; en üstte çıkartmanın düğmeleri. Çizgiler ayrı tutulduğu için fırça onları
 * örtmez, silgi silmez; boya kovası onları duvar sayar. Çıkartma ✓ ile boya katmanına işlenir.
 *
 * Tüm metotlar ana iş parçacığından çağrılır. Yalnızca boya kovasının piksel hesabı arka planda
 * yapılır; bu sırada yeni çizgi ve geri alma kabul edilmez, böylece bitmap tutarlı kalır.
 */
@ViewModelScoped
class DrawingEngine @Inject constructor(
    private val strokeFactory: StrokeFactory,
    private val stickerRenderer: StickerRenderer,
    private val floodFiller: FloodFiller,
    @param:DefaultDispatcher private val fillDispatcher: CoroutineDispatcher,
) {
    private val surface = DrawingSurface()
    private val stickerEditor = StickerEditor(stickerRenderer.metrics)
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

    /** [zoom], çıkartma düğmelerinin ekranda hep aynı boyda görünmesi için kullanılır. */
    fun draw(canvas: Canvas, zoom: Float) {
        canvas.drawColor(backgroundColor)
        surface.drawTo(canvas)
        activeStroke?.drawPreview(canvas)
        val sticker = stickerEditor.draft
        sticker?.let { stickerRenderer.drawSticker(canvas, it) }
        currentLineArt()?.let { canvas.drawBitmap(it.bitmap, 0f, 0f, null) }
        sticker?.let { stickerRenderer.drawControls(canvas, it, zoom) }
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
        commitToLayer(stroke.dirtyRegion(), stroke::commit)
    }

    fun cancelStroke() {
        if (activeStroke == null) return
        activeStroke = null
        notifyChanged()
    }

    /** Çıkartma dokunuşu; onay düğmesine dokunulduysa çıkartma resme işlenir. */
    /** Dokunuş ✓ düğmesine geldiyse çıkartma resme işlenir ve `true` döner. */
    fun startStickerTouch(emoji: String, x: Float, y: Float, zoom: Float): Boolean {
        if (isFilling) return false
        val result = stickerEditor.touchStart(emoji, x, y, zoom)
        if (result == StickerTouchResult.CONFIRMED) return commitSticker()
        notifyChanged()
        return false
    }

    fun moveStickerTouch(x: Float, y: Float) = stickerEditor.touchMove(x, y)

    fun endStickerTouch() = stickerEditor.touchEnd()

    fun changeStickerEmoji(emoji: String) {
        stickerEditor.changeEmoji(emoji)
        notifyChanged()
    }

    /**
     * Düzenlenen çıkartma varsa resme işler. Onay düğmesinin yanında araç değişince, kayıttan ve
     * dışa aktarmadan önce de çağrılır; böylece konan çıkartma kaybolmaz. İşlendiyse `true` döner.
     */
    fun commitSticker(): Boolean {
        if (isFilling) return false
        val draft = stickerEditor.take() ?: return false
        commitToLayer(stickerRenderer.bounds(draft)) { stickerRenderer.drawSticker(it, draft) }
        return true
    }

    /** Bir bölge boyandıysa `true` döner; dokunulan yer zaten o renkse veya boyama yarıda kaldıysa `false`. */
    suspend fun fill(x: Float, y: Float, color: Int): Boolean {
        if (isFilling) return false
        val width = surface.width
        val height = surface.height
        isFilling = true
        try {
            val pixels = surface.readPixels()
            val walls = currentLineArt()?.walls
            val bounds = withContext(fillDispatcher) {
                floodFiller.fill(pixels, width, height, x.toInt(), y.toInt(), color, walls)
            } ?: return false
            if (surface.width != width || surface.height != height) return false
            surface.snapshot(bounds.toRect())?.let(history::record)
            surface.writePixels(pixels, bounds)
            markContentChanged()
            publishHistory()
            notifyChanged()
            return true
        } finally {
            isFilling = false
        }
    }

    /** Düzenlenen bir çıkartma varsa geri alma önce onu kaldırır. */
    fun undo() {
        if (stickerEditor.take() != null) return notifyChanged()
        applyHistory { history.undo(surface::restore) }
    }

    fun redo() = applyHistory { history.redo(surface::restore) }

    fun release() {
        onChanged = null
        stickerEditor.take()
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

    /** Bölgenin önceki hâlini geçmişe kaydeder, sonra [block] ile boya katmanına çizer. */
    private fun commitToLayer(region: Rect, block: (Canvas) -> Unit) {
        surface.snapshot(region)?.let { before ->
            history.record(before)
            surface.paint(block)
            markContentChanged()
        }
        publishHistory()
        notifyChanged()
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
