package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.annotation.StringRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.ToolType

/** Araçların panelde görünen simgesi (emoji) ve erişilebilirlik etiketi. */
val ToolType.emoji: String
    get() = when (this) {
        ToolType.PENCIL -> "✏️"
        ToolType.BRUSH -> "🖌️"
        ToolType.RAINBOW -> "🌈"
        ToolType.MARKER -> "🖊️"
        ToolType.CRAYON -> "🖍️"
        ToolType.SPRAY -> "💨"
        ToolType.ERASER -> "🧽"
        ToolType.BUCKET -> "🪣"
    }

@get:StringRes
val ToolType.labelRes: Int
    get() = when (this) {
        ToolType.PENCIL -> R.string.tool_pencil
        ToolType.BRUSH -> R.string.tool_brush
        ToolType.RAINBOW -> R.string.tool_rainbow
        ToolType.MARKER -> R.string.tool_marker
        ToolType.CRAYON -> R.string.tool_crayon
        ToolType.SPRAY -> R.string.tool_spray
        ToolType.ERASER -> R.string.tool_eraser
        ToolType.BUCKET -> R.string.tool_bucket
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
