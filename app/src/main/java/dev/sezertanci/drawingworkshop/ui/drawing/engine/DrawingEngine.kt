package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
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
 * Tüm metotlar ana iş parçacığından çağrılır. Yalnızca boya kovasının piksel hesabı arka planda
 * yapılır; bu sırada yeni çizgi ve geri alma kabul edilmez, böylece bitmap tutarlı kalır.
 */
class DrawingEngine @Inject constructor(
    private val strokeFactory: StrokeFactory,
    private val floodFiller: FloodFiller,
    @param:DefaultDispatcher private val fillDispatcher: CoroutineDispatcher,
) {
    private val surface = DrawingSurface()
    private val history = UndoRedoHistory<RegionSnapshot>(UNDO_CAPACITY, RegionSnapshot::release)
    private val _historyState = MutableStateFlow(HistoryState(canUndo = false, canRedo = false))
    private var activeStroke: ActiveStroke? = null
    private var isFilling = false
    private var onChanged: (() -> Unit)? = null

    val historyState: StateFlow<HistoryState> = _historyState.asStateFlow()

    var settings: StrokeSettings = StrokeSettings(ToolType.BRUSH, BrushSize.MEDIUM, Palette.RED)

    var backgroundColor: Int = Palette.WHITE
        set(value) {
            field = value
            notifyChanged()
        }

    /** Görünüm, yeniden çizilmesi gerektiğinde haber alır. Motor görünümden uzun yaşadığı için `null` ile bırakılır. */
    fun setOnChangedListener(listener: (() -> Unit)?) {
        onChanged = listener
    }

    fun resize(width: Int, height: Int) {
        cancelStroke()
        surface.resize(width, height)
        notifyChanged()
    }

    fun draw(canvas: Canvas) {
        canvas.drawColor(backgroundColor)
        surface.drawTo(canvas)
        activeStroke?.drawPreview(canvas)
    }

    fun startStroke(x: Float, y: Float) {
        if (isFilling) return
        activeStroke = strokeFactory.create(settings, backgroundColor)?.also { it.begin(x, y) }
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
            val bounds = withContext(fillDispatcher) {
                floodFiller.fill(pixels, width, height, x.toInt(), y.toInt(), color)
            } ?: return
            if (surface.width != width || surface.height != height) return
            surface.snapshot(bounds.toRect())?.let(history::record)
            surface.writePixels(pixels, bounds)
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
    }

    private fun applyHistory(action: () -> Boolean) {
        if (isFilling || activeStroke != null) return
        if (action()) {
            publishHistory()
            notifyChanged()
        }
    }

    private fun publishHistory() {
        _historyState.value = HistoryState(history.canUndo, history.canRedo)
    }

    private fun notifyChanged() {
        onChanged?.invoke()
    }

    private companion object {
        const val UNDO_CAPACITY = 5
    }
}
