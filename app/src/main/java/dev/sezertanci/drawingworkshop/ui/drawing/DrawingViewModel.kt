package dev.sezertanci.drawingworkshop.ui.drawing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.StrokeSettings
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.ui.drawing.engine.DrawingEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Çizim ekranının durumunu tutar ve paneller ile tuvali motora bağlar.
 * Motor bu ViewModel'e aittir; ekran döndüğünde veya yeniden oluşturulduğunda çizim kaybolmaz.
 */
@HiltViewModel
class DrawingViewModel @Inject constructor(
    val engine: DrawingEngine,
) : ViewModel(), DrawingTouchListener {

    private val selection = MutableStateFlow(DrawingUiState())

    val uiState: StateFlow<DrawingUiState> = combine(selection, engine.historyState) { state, history ->
        state.copy(canUndo = history.canUndo, canRedo = history.canRedo)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, selection.value)

    init {
        applyToEngine(selection.value)
    }

    fun selectTool(tool: ToolType) = updateSelection { it.copy(tool = tool) }

    fun selectBrushSize(size: BrushSize) = updateSelection { it.copy(brushSize = size) }

    fun selectColorTarget(target: ColorTarget) = updateSelection { it.copy(colorTarget = target) }

    fun selectColor(color: Int) = updateSelection {
        when (it.colorTarget) {
            ColorTarget.STROKE -> it.copy(strokeColor = color)
            ColorTarget.BACKGROUND -> it.copy(backgroundColor = color)
        }
    }

    fun undo() = engine.undo()

    fun redo() = engine.redo()

    override fun onTouchStart(x: Float, y: Float) {
        val state = selection.value
        if (state.tool == ToolType.BUCKET) {
            viewModelScope.launch { engine.fill(x, y, state.strokeColor) }
        } else {
            engine.startStroke(x, y)
        }
    }

    override fun onTouchMove(x: Float, y: Float) = engine.extendStroke(x, y)

    override fun onTouchBatchEnd() = engine.invalidate()

    override fun onTouchEnd() = engine.endStroke()

    override fun onTouchCancel() = engine.cancelStroke()

    override fun onCleared() {
        engine.release()
    }

    private fun updateSelection(transform: (DrawingUiState) -> DrawingUiState) {
        selection.update(transform)
        applyToEngine(selection.value)
    }

    private fun applyToEngine(state: DrawingUiState) {
        engine.settings = StrokeSettings(state.tool, state.brushSize, state.strokeColor)
        engine.backgroundColor = state.backgroundColor
    }
}
