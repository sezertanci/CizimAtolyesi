package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.symmetry.SymmetryMode
import dev.sezertanci.drawingworkshop.ui.components.DrawableIcon
import dev.sezertanci.drawingworkshop.ui.components.EmojiIcon
import dev.sezertanci.drawingworkshop.ui.components.PanelButton
import dev.sezertanci.drawingworkshop.ui.theme.WorkshopColors

private val IconSize = 36.dp
private val IconFrameWidth = 2.dp
private val IconAxisWidth = 3.dp
private val IconCornerRadius = 6.dp
private val MenuItemPadding = 6.dp
private val BadgeSize = 13.sp

/** Araç panelindeki simetri seçimi ve kova seçiliyken simetrik / tek doldurma seçimi. */
class SymmetryControls(
    val mode: SymmetryMode,
    val symmetricFill: Boolean,
    val showFillChoice: Boolean,
    val onModeSelected: (SymmetryMode) -> Unit,
    val onSymmetricFillSelected: (Boolean) -> Unit,
)

/** Simetri düğmesini ve gerekiyorsa doldurma seçimini panele ekler. */
internal fun LazyListScope.symmetryItems(controls: SymmetryControls) {
    item { SymmetryMenuButton(controls.mode, controls.onModeSelected) }
    if (controls.showFillChoice) {
        item {
            FillChoiceButton(
                copies = controls.mode.transforms.size,
                selected = controls.symmetricFill,
                descriptionRes = R.string.fill_symmetric,
                onClick = { controls.onSymmetricFillSelected(true) },
            )
        }
        item {
            FillChoiceButton(
                copies = 1,
                selected = !controls.symmetricFill,
                descriptionRes = R.string.fill_single,
                onClick = { controls.onSymmetricFillSelected(false) },
            )
        }
    }
}

/** Simetri kapalıyken iki yarısı aynı kelebek simgesi, açıkken seçili modun eksenleri görünür. Dokununca seçim kutusu açılır. */
@Composable
private fun SymmetryMenuButton(mode: SymmetryMode, onModeSelected: (SymmetryMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        PanelButton(
            selected = mode != SymmetryMode.OFF,
            description = stringResource(R.string.symmetry_menu),
            onClick = { expanded = true },
        ) { if (mode == SymmetryMode.OFF) DrawableIcon(R.drawable.ic_symmetry) else SymmetryIcon(mode) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SymmetryMode.entries.forEach { option ->
                PanelButton(
                    selected = option == mode,
                    description = stringResource(option.labelRes),
                    onClick = {
                        expanded = false
                        onModeSelected(option)
                    },
                    modifier = Modifier.padding(MenuItemPadding),
                ) { if (option == SymmetryMode.OFF) EmojiIcon(OFF_EMOJI) else SymmetryIcon(option) }
            }
        }
    }
}

/** Kova ve kaç yerin dolacağını gösteren küçük sayı (×4, ×1). */
@Composable
private fun FillChoiceButton(copies: Int, selected: Boolean, @StringRes descriptionRes: Int, onClick: () -> Unit) {
    PanelButton(selected = selected, description = stringResource(descriptionRes), onClick = onClick) {
        Box {
            DrawableIcon(R.drawable.ic_tool_bucket)
            Text(
                text = "×$copies",
                fontSize = BadgeSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}

/** Kare bir çerçeve ve içinde modun ayna eksenleri. */
@Composable
private fun SymmetryIcon(mode: SymmetryMode) {
    Canvas(Modifier.size(IconSize)) {
        drawRoundRect(
            color = WorkshopColors.Outline,
            cornerRadius = CornerRadius(IconCornerRadius.toPx()),
            style = Stroke(IconFrameWidth.toPx()),
        )
        val half = size.minDimension / 2f
        for (axis in mode.axes) {
            val offset = Offset(axis.dx * half, axis.dy * half)
            drawLine(
                color = WorkshopColors.Selection,
                start = center - offset,
                end = center + offset,
                strokeWidth = IconAxisWidth.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@get:StringRes
private val SymmetryMode.labelRes: Int
    get() = when (this) {
        SymmetryMode.OFF -> R.string.symmetry_off
        SymmetryMode.LEFT_RIGHT -> R.string.symmetry_left_right
        SymmetryMode.TOP_BOTTOM -> R.string.symmetry_top_bottom
        SymmetryMode.FOUR -> R.string.symmetry_four
        SymmetryMode.EIGHT -> R.string.symmetry_eight
    }

private const val OFF_EMOJI = "🚫"
