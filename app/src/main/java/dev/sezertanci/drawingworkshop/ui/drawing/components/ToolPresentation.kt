package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.ui.components.DrawableIcon
import dev.sezertanci.drawingworkshop.ui.components.EmojiIcon

/** Aracın panelde görünen simgesi: uygun emojisi olmayan araçlar için çizim kullanılır. */
sealed interface ToolIcon {
    data class Emoji(val text: String) : ToolIcon
    data class Drawable(@param:DrawableRes val res: Int) : ToolIcon
}

/** Araçların panelde görünen simgesi ve erişilebilirlik etiketi. */
val ToolType.icon: ToolIcon
    get() = when (this) {
        ToolType.PENCIL -> ToolIcon.Emoji("✏️")
        ToolType.BRUSH -> ToolIcon.Emoji("🖌️")
        ToolType.RAINBOW -> ToolIcon.Emoji("🌈")
        ToolType.SILK -> ToolIcon.Drawable(R.drawable.ic_tool_silk)
        ToolType.MARKER -> ToolIcon.Drawable(R.drawable.ic_tool_marker)
        ToolType.CRAYON -> ToolIcon.Emoji("🖍️")
        ToolType.SPRAY -> ToolIcon.Drawable(R.drawable.ic_tool_spray)
        ToolType.ERASER -> ToolIcon.Drawable(R.drawable.ic_tool_eraser)
        ToolType.BUCKET -> ToolIcon.Drawable(R.drawable.ic_tool_bucket)
        ToolType.STICKER -> ToolIcon.Emoji("🐱")
    }

@Composable
fun ToolIconView(icon: ToolIcon) {
    when (icon) {
        is ToolIcon.Emoji -> EmojiIcon(icon.text)
        is ToolIcon.Drawable -> DrawableIcon(icon.res)
    }
}

@get:StringRes
val ToolType.labelRes: Int
    get() = when (this) {
        ToolType.PENCIL -> R.string.tool_pencil
        ToolType.BRUSH -> R.string.tool_brush
        ToolType.RAINBOW -> R.string.tool_rainbow
        ToolType.SILK -> R.string.tool_silk
        ToolType.MARKER -> R.string.tool_marker
        ToolType.CRAYON -> R.string.tool_crayon
        ToolType.SPRAY -> R.string.tool_spray
        ToolType.ERASER -> R.string.tool_eraser
        ToolType.BUCKET -> R.string.tool_bucket
        ToolType.STICKER -> R.string.tool_sticker
    }

@get:StringRes
val BrushSize.labelRes: Int
    get() = when (this) {
        BrushSize.SMALL -> R.string.size_small
        BrushSize.MEDIUM -> R.string.size_medium
        BrushSize.LARGE -> R.string.size_large
    }

/** Boyut düğmesindeki noktanın çapı. */
val BrushSize.previewDiameter: Dp
    get() = when (this) {
        BrushSize.SMALL -> 10.dp
        BrushSize.MEDIUM -> 20.dp
        BrushSize.LARGE -> 34.dp
    }
