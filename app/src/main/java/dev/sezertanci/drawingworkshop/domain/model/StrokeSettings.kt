package dev.sezertanci.drawingworkshop.domain.model

/**
 * Yeni bir çizgi başlarken kullanılacak araç, boyut, renk ve ipek fırçasının stili. [rainbow] açıksa
 * çizgi [color] yerine çizgi boyunca değişen gökkuşağı renkleriyle çizilir.
 */
data class StrokeSettings(
    val tool: ToolType,
    val size: BrushSize,
    val color: Int,
    val silkStyle: SilkStyle = SilkStyle(),
    val rainbow: Boolean = false,
)
