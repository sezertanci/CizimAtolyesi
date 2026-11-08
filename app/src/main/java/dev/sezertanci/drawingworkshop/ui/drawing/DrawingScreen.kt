package dev.sezertanci.drawingworkshop.ui.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.ui.drawing.components.ColorPanel
import dev.sezertanci.drawingworkshop.ui.drawing.components.ToolPanel

private val CanvasPadding = 8.dp

/** Çizim ekranının giriş noktası: ViewModel'i alır ve durumu ekrana bağlar. */
@Composable
fun DrawingRoute(viewModel: DrawingViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DrawingScreen(
        state = state,
        canvas = { modifier ->
            AndroidView(
                factory = ::DrawingView,
                update = { it.bind(viewModel.engine, viewModel) },
                modifier = modifier,
            )
        },
        onToolSelected = viewModel::selectTool,
        onSizeSelected = viewModel::selectBrushSize,
        onUndo = viewModel::undo,
        onRedo = viewModel::redo,
        onColorTargetSelected = viewModel::selectColorTarget,
        onColorSelected = viewModel::selectColor,
    )
}

/** Yatay yerleşim: solda araç paneli, ortada tuval, sağda renk paneli. */
@Composable
fun DrawingScreen(
    state: DrawingUiState,
    canvas: @Composable (Modifier) -> Unit,
    onToolSelected: (ToolType) -> Unit,
    onSizeSelected: (BrushSize) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onColorTargetSelected: (ColorTarget) -> Unit,
    onColorSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.panel_background))
            .windowInsetsPadding(WindowInsets.displayCutout),
    ) {
        ToolPanel(
            selectedTool = state.tool,
            selectedSize = state.brushSize,
            strokeColor = Color(state.strokeColor),
            canUndo = state.canUndo,
            canRedo = state.canRedo,
            onToolSelected = onToolSelected,
            onSizeSelected = onSizeSelected,
            onUndo = onUndo,
            onRedo = onRedo,
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
