package dev.sezertanci.drawingworkshop.ui.drawing

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.ui.components.screenContainer
import dev.sezertanci.drawingworkshop.ui.drawing.components.ColorPanel
import dev.sezertanci.drawingworkshop.ui.drawing.components.ToolPanel
import dev.sezertanci.drawingworkshop.ui.drawing.components.ToolPanelActions

private val CanvasPadding = 8.dp

/** Çizim ekranının giriş noktası: ViewModel'i alır ve durumu ekrana bağlar. */
@Composable
fun DrawingRoute(onHome: () -> Unit, viewModel: DrawingViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.saveNow() }
    DrawingScreen(
        state = state,
        canvas = { modifier ->
            AndroidView(
                factory = ::DrawingView,
                update = { it.bind(viewModel.engine, viewModel) },
                modifier = modifier,
            )
        },
        actions = ToolPanelActions(
            canUndo = state.canUndo,
            canRedo = state.canRedo,
            exportStatus = state.exportStatus,
            onHome = onHome,
            onUndo = viewModel::undo,
            onRedo = viewModel::redo,
            onExport = viewModel::export,
        ),
        onToolSelected = viewModel::selectTool,
        onSizeSelected = viewModel::selectBrushSize,
        onColorTargetSelected = viewModel::selectColorTarget,
        onColorSelected = viewModel::selectColor,
    )
}

/** Yatay yerleşim: solda araç paneli, ortada tuval, sağda renk paneli. */
@Composable
fun DrawingScreen(
    state: DrawingUiState,
    canvas: @Composable (Modifier) -> Unit,
    actions: ToolPanelActions,
    onToolSelected: (ToolType) -> Unit,
    onSizeSelected: (BrushSize) -> Unit,
    onColorTargetSelected: (ColorTarget) -> Unit,
    onColorSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.screenContainer(),
    ) {
        ToolPanel(
            selectedTool = state.tool,
            selectedSize = state.brushSize,
            strokeColor = Color(state.strokeColor),
            actions = actions,
            onToolSelected = onToolSelected,
            onSizeSelected = onSizeSelected,
        )
        canvas(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = CanvasPadding),
        )
        ColorPanel(
            colorTarget = state.colorTarget,
            selectedColor = state.selectedColor,
            onColorTargetSelected = onColorTargetSelected,
            onColorSelected = onColorSelected,
        )
    }
}
