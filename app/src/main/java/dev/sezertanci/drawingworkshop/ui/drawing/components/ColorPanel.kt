package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.model.Palette
import dev.sezertanci.drawingworkshop.ui.components.EmojiIcon
import dev.sezertanci.drawingworkshop.ui.components.PanelButton
import dev.sezertanci.drawingworkshop.ui.drawing.ColorTarget

private val SwatchSize = 52.dp

/**
 * Sağdaki kaydırılabilir renk paneli. Üstteki iki düğme, seçilen rengin kaleme mi yoksa tuvalin
 * arka planına mı uygulanacağını belirler.
 */
@Composable
fun ColorPanel(
    colorTarget: ColorTarget,
    selectedColor: Int,
    onColorTargetSelected: (ColorTarget) -> Unit,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PanelColumn(width = RightPanelWidth, modifier = modifier) {
        item {
            PanelButton(
                selected = colorTarget == ColorTarget.STROKE,
                description = stringResource(R.string.color_target_stroke),
                onClick = { onColorTargetSelected(ColorTarget.STROKE) },
            ) { EmojiIcon(STROKE_TARGET_EMOJI) }
        }
        item {
            PanelButton(
                selected = colorTarget == ColorTarget.BACKGROUND,
                description = stringResource(R.string.color_target_background),
                onClick = { onColorTargetSelected(ColorTarget.BACKGROUND) },
            ) { EmojiIcon(BACKGROUND_TARGET_EMOJI) }
        }
        item { PanelDivider() }
        items(Palette.colors) { color ->
            PanelButton(
                selected = color == selectedColor,
                description = stringResource(R.string.color_swatch),
                onClick = { onColorSelected(color) },
                size = SwatchSize,
                shape = CircleShape,
                background = Color(color),
            ) {}
        }
    }
}

private const val STROKE_TARGET_EMOJI = "🖌️"
private const val BACKGROUND_TARGET_EMOJI = "🖼️"
