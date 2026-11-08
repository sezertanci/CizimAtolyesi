package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.domain.stroke.RainbowCycle
import dev.sezertanci.drawingworkshop.ui.components.EmojiIcon
import dev.sezertanci.drawingworkshop.ui.components.PanelButton
import dev.sezertanci.drawingworkshop.ui.drawing.ExportStatus

private val PanelWidth = 88.dp
private val PanelDividerWidth = 56.dp
private val PanelDividerThickness = 2.dp

/** Gökkuşağı fırçası seçiliyken boyut noktaları tek renk yerine gökkuşağı renkleriyle görünür. */
private val RainbowDotBrush = Brush.sweepGradient(
    (0..RainbowCycle.STEPS).map { Color(RainbowCycle.colorOf(it)) },
)

/** Panelin araç dışındaki düğmeleri: galeriye dönme, geri al / yinele, temizleme ve galeriye aktarma. */
class ToolPanelActions(
    val canUndo: Boolean,
    val canRedo: Boolean,
    val exportStatus: ExportStatus,
    val onHome: () -> Unit,
    val onUndo: () -> Unit,
    val onRedo: () -> Unit,
    val onClear: () -> Unit,
    val onExport: () -> Unit,
)

/** Soldaki kaydırılabilir araç paneli: galeri, araçlar, fırça boyutları, simetri, geri al, yinele, temizle ve aktarma. */
@Composable
fun ToolPanel(
    selectedTool: ToolType,
    selectedSize: BrushSize,
    strokeColor: Color,
    actions: ToolPanelActions,
    symmetry: SymmetryControls,
    onToolSelected: (ToolType) -> Unit,
    onSizeSelected: (BrushSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmingClear by rememberSaveable { mutableStateOf(false) }
    val dotBrush = if (selectedTool == ToolType.RAINBOW) RainbowDotBrush else SolidColor(strokeColor)
    PanelColumn(width = PanelWidth, modifier = modifier) {
        item { ActionButton(HOME_EMOJI, R.string.action_home, onClick = actions.onHome) }
        item { PanelDivider() }
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
            ) { Box(Modifier.size(size.previewDiameter).background(dotBrush, CircleShape)) }
        }
        item { PanelDivider() }
        symmetryItems(symmetry)
        item { PanelDivider() }
        item { ActionButton(UNDO_EMOJI, R.string.action_undo, actions.canUndo, actions.onUndo) }
        item { ActionButton(REDO_EMOJI, R.string.action_redo, actions.canRedo, actions.onRedo) }
        item { ActionButton(CLEAR_EMOJI, R.string.action_clear) { confirmingClear = true } }
        item { PanelDivider() }
        item {
            val running = actions.exportStatus == ExportStatus.RUNNING
            ActionButton(actions.exportStatus.emoji, R.string.action_export, !running, actions.onExport)
        }
    }
    if (confirmingClear) {
        ClearConfirmDialog(
            onConfirm = {
                confirmingClear = false
                actions.onClear()
            },
            onDismiss = { confirmingClear = false },
        )
    }
}

@Composable
private fun ActionButton(emoji: String, descriptionRes: Int, enabled: Boolean = true, onClick: () -> Unit) {
    PanelButton(
        selected = false,
        enabled = enabled,
        description = stringResource(descriptionRes),
        onClick = onClick,
    ) { EmojiIcon(emoji) }
}

@Composable
internal fun PanelDivider() {
    HorizontalDivider(modifier = Modifier.width(PanelDividerWidth), thickness = PanelDividerThickness)
}

private val ExportStatus.emoji: String
    get() = when (this) {
        ExportStatus.IDLE -> "📤"
        ExportStatus.RUNNING -> "⏳"
        ExportStatus.DONE -> "✅"
        ExportStatus.FAILED -> "⚠️"
    }

private const val HOME_EMOJI = "🏠"
private const val UNDO_EMOJI = "↩️"
private const val REDO_EMOJI = "↪️"
