package dev.sezertanci.drawingworkshop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sezertanci.drawingworkshop.ui.theme.WorkshopColors

private val ButtonSize = 64.dp
private val SelectedBorder = 4.dp
private val NormalBorder = 1.dp
private const val DISABLED_ALPHA = 0.35f
private val DefaultShape = RoundedCornerShape(16.dp)
private val EmojiSize = 30.sp

/** Panellerdeki tüm düğmelerin ortak görünümü: seçiliyse turuncu çerçeve, pasifse soluk. */
@Composable
fun PanelButton(
    selected: Boolean,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = ButtonSize,
    shape: Shape = DefaultShape,
    background: Color = if (selected) WorkshopColors.SelectionBackground else WorkshopColors.ItemBackground,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(shape)
            .background(background)
            .border(
                width = if (selected) SelectedBorder else NormalBorder,
                color = if (selected) WorkshopColors.Selection else WorkshopColors.Outline,
                shape = shape,
            )
            .clickableWithSound(enabled = enabled, onClick = onClick)
            .semantics {
                contentDescription = description
                this.selected = selected
            },
    ) {
        content()
    }
}

/** Düğmelerdeki emoji simgesi. */
@Composable
fun EmojiIcon(emoji: String) {
    Text(text = emoji, fontSize = EmojiSize)
}
