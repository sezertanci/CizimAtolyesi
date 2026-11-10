package dev.sezertanci.drawingworkshop.domain.model

/** Yeni bir çizgi başlarken kullanılacak araç, boyut, renk ve ipek fırçasının stili. */
data class StrokeSettings(
    val tool: ToolType,
    val size: BrushSize,
    val color: Int,
    val silkStyle: SilkStyle = SilkStyle.DEFAULT,
)
