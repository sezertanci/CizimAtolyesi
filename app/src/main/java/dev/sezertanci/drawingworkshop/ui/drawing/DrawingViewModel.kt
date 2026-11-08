package dev.sezertanci.drawingworkshop.ui.drawing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sezertanci.drawingworkshop.data.DrawingRepository
import dev.sezertanci.drawingworkshop.data.export.DrawingExporter
import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.StrokeSettings
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.ui.drawing.engine.DrawingEngine
import dev.sezertanci.drawingworkshop.ui.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * Çizim ekranının durumunu tutar ve paneller ile tuvali motora bağlar.
 * Motor bu ViewModel'e aittir; ekran döndüğünde veya yeniden oluşturulduğunda çizim kaybolmaz.
 *
 * Açılışta resim kayıttan yüklenir; yükleme bitene kadar tuval dokunmaları kabul etmez.
 */
@HiltViewModel
class DrawingViewModel @Inject constructor(
    val engine: DrawingEngine,
    private val repository: DrawingRepository,
    private val autoSaver: AutoSaver,
    private val exporter: DrawingExporter,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), DrawingTouchListener {

    private val selection = MutableStateFlow(DrawingUiState())

    /** Yeni resmin kimliği de saklanır; uygulama arka planda kapatılıp açılırsa aynı resim yüklenir. */
    private val drawingId: String = savedStateHandle.get<String>(Routes.ARG_DRAWING_ID)
        ?: UUID.randomUUID().toString().also { savedStateHandle[Routes.ARG_DRAWING_ID] = it }

    val uiState: StateFlow<DrawingUiState> = combine(selection, engine.historyState) { state, history ->
        state.copy(canUndo = history.canUndo, canRedo = history.canRedo)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, selection.value)

    init {
        applyToEngine(selection.value)
        viewModelScope.launch { loadDrawing() }
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

    fun saveNow() = autoSaver.saveNow()

    fun export() {
        if (selection.value.exportStatus == ExportStatus.RUNNING) return
        val layer = engine.copyLayer() ?: return
        val backgroundColor = engine.backgroundColor
        setExportStatus(ExportStatus.RUNNING)
        viewModelScope.launch {
            val exported = exporter.export(layer, backgroundColor)
            setExportStatus(if (exported) ExportStatus.DONE else ExportStatus.FAILED)
            delay(EXPORT_FEEDBACK_DURATION)
            setExportStatus(ExportStatus.IDLE)
        }
    }

    override fun onTouchStart(x: Float, y: Float) {
        val state = selection.value
        if (!state.isLoaded) return
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

    /** Ekrandan çıkılırken son değişiklikler kaydedilir; kopya alındıktan sonra motor bırakılır. */
    override fun onCleared() {
        autoSaver.saveNow()
        engine.release()
    }

    private suspend fun loadDrawing() {
        repository.load(drawingId)?.let { stored ->
            stored.layer?.let(engine::load)
            updateSelection { it.copy(backgroundColor = stored.backgroundColor) }
        }
        autoSaver.start(drawingId, viewModelScope)
        selection.update { it.copy(isLoaded = true) }
    }

    private fun setExportStatus(status: ExportStatus) = selection.update { it.copy(exportStatus = status) }

    private fun updateSelection(transform: (DrawingUiState) -> DrawingUiState) {
        selection.update(transform)
        applyToEngine(selection.value)
    }

    private fun applyToEngine(state: DrawingUiState) {
        engine.settings = StrokeSettings(state.tool, state.brushSize, state.strokeColor)
        engine.backgroundColor = state.backgroundColor
    }

    private companion object {
        val EXPORT_FEEDBACK_DURATION = 2.seconds
    }
}
