package dev.sezertanci.drawingworkshop.domain.model

/** Üç fırça boyutu. Genişlik dp cinsindendir; her araç bu değeri kendi katsayısıyla ölçekler. */
enum class BrushSize(val widthDp: Float) {
    SMALL(6f),
    MEDIUM(14f),
    LARGE(28f),
}
