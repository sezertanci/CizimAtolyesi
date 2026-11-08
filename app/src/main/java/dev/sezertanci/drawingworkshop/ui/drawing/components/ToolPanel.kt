package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.ToolType

private val PanelWidth = 88.dp
private val PanelPadding = 12.dp
private val ItemSpacing = 10.dp
private val EmojiSize = 30.sp

/** Soldaki kaydırılabilir araç paneli: araçlar, fırça boyutları, geri al ve yinele. */
@Composable
fun ToolPanel(
    selectedTool: ToolType,
    selectedSize: BrushSize,
    strokeColor: Color,
    canUndo: Boolean,
    canRedo: Boolean,
    onToolSelected: (ToolType) -> Unit,
    onSizeSelected: (BrushSize) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.width(PanelWidth).fillMaxHeight(),
        contentPadding = PaddingValues(vertical = PanelPadding),
        verticalArrangement = Arrangement.spacedBy(ItemSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(ToolType.entries) { tool ->
            PanelButton(
                selected = tool == selectedTool,
                description = stringResource(tool.labelRes),
                onClick = { onToolSelected(tool) },
            ) { EmojiIcon(tool.emoji) }
        }
        item { PanelDivider() }
        items(BrushSize.entries) { size ->
            PanelButton(
                selected = size == selectedSize,
                description = stringResource(size.labelRes),
                onClick = { onSizeSelected(size) },
            ) { Box(Modifier.size(size.previewDiameter).background(strokeColor, CircleShape)) }
        }
        item { PanelDivider() }
        item {
            PanelButton(
                selected = false,
                enabled = canUndo,
                description = stringResource(R.string.action_undo),
                onClick = onUndo,
            ) { EmojiIcon(UNDO_EMOJI) }
        }
        item {
            PanelButton(
                selected = false,
                enabled = canRedo,
                description = stringResource(R.string.action_redo),
                onClick = onRedo,
            ) { EmojiIcon(REDO_EMOJI) }
        }
    }
}

@Composable
internal fun EmojiIcon(emoji: String) {
    Text(text = emoji, fontSize = EmojiSize)
}

@Composable
internal fun PanelDivider() {
    HorizontalDivider(modifier = Modifier.width(PanelDividerWidth), thickness = 2.dp)
}

private val PanelDividerWidth = 56.dp
private const val UNDO_EMOJI = "↩️"
private const val REDO_EMOJI = "↪️"
