package dev.sezertanci.drawingworkshop.domain.model

/** Yeni bir çizgi başlarken kullanılacak araç, boyut ve renk. */
data class StrokeSettings(
    val tool: ToolType,
    val size: BrushSize,
    val color: Int,
)
