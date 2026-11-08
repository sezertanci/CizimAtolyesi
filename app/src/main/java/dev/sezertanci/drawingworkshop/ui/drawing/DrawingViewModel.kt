package dev.sezertanci.drawingworkshop.ui.drawing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sezertanci.drawingworkshop.data.DrawingRepository
import dev.sezertanci.drawingworkshop.data.coloring.ColoringPageCatalog
import dev.sezertanci.drawingworkshop.data.export.DrawingExporter
import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.StrokeSettings
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.ui.drawing.engine.DrawingEngine
import dev.sezertanci.drawingworkshop.ui.drawing.engine.LineArtFactory
import dev.sezertanci.drawingworkshop.ui.navigation.Routes
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
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
 * Açılışta resim kayıttan yüklenir; resim bir boyama sayfasından açıldıysa sayfanın çizgileri de
 * hazırlanır. Yükleme bitene kadar tuval dokunmaları kabul etmez. Boyama sayfasından açılan resim,
 * üzerinde bir değişiklik yapılmadıkça kaydedilmez; böylece galeride boş resim birikmez.
 */
@HiltViewModel
class DrawingViewModel @Inject constructor(
    val engine: DrawingEngine,
    private val repository: DrawingRepository,
    private val autoSaver: AutoSaver,
    private val exporter: DrawingExporter,
    private val pageCatalog: ColoringPageCatalog,
    private val lineArtFactory: LineArtFactory,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), DrawingTouchListener {

    private val selection = MutableStateFlow(DrawingUiState())

    /** Yeni resmin kimliği de saklanır; uygulama arka planda kapatılıp açılırsa aynı resim yüklenir. */
    private val drawingId: String = savedStateHandle.get<String>(Routes.ARG_DRAWING_ID)
        ?: UUID.randomUUID().toString().also { savedStateHandle[Routes.ARG_DRAWING_ID] = it }

    /** Yeni resim için seçilen boyama sayfası; kayıtlı resimde kayıttaki sayfa geçerlidir. */
    private val requestedTemplateId: String? = savedStateHandle[Routes.ARG_TEMPLATE_ID]
    private var templateId: String? = null

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
        val state = selection.value
        if (!state.isLoaded || state.exportStatus == ExportStatus.RUNNING) return
        val layer = engine.copyLayer() ?: return
        val backgroundColor = engine.backgroundColor
        setExportStatus(ExportStatus.RUNNING)
        viewModelScope.launch {
            val exported = exporter.export(layer, backgroundColor, templateId)
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
        val stored = repository.load(drawingId)
        stored?.let {
            it.layer?.let(engine::load)
            updateSelection { state -> state.copy(backgroundColor = it.backgroundColor) }
        }
        val pageId = (stored?.templateId ?: requestedTemplateId)?.takeIf { pageCatalog.find(it) != null }
        pageId?.let { attachLineArt(it) }
        templateId = pageId
        autoSaver.start(drawingId, pageId, viewModelScope)
        selection.update { it.copy(isLoaded = true) }
    }

    /**
     * Çizgiler tuval boyutunda hazırlanır; tuval yeniden boyutlanırsa yeniden hazırlanır.
     * İlk hazırlık bitene kadar bekler; böylece boya kovası çizgileri duvar olarak görmeden çalışmaz.
     */
    private suspend fun attachLineArt(pageId: String) {
        val ready = CompletableDeferred<Unit>()
        viewModelScope.launch {
            engine.canvasSize.filterNotNull().collectLatest { size ->
                engine.setLineArt(lineArtFactory.create(pageId, size.width, size.height))
                ready.complete(Unit)
            }
        }
        ready.await()
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
