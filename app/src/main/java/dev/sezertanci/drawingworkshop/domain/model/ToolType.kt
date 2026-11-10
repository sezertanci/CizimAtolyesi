package dev.sezertanci.drawingworkshop.domain.model

/** Araç panelinde seçilebilen çizim araçları. */
enum class ToolType {
    PENCIL,
    BRUSH,
    RAINBOW,
    MARKER,
    CRAYON,
    SPRAY,
    ERASER,
    BUCKET,
    STICKER,
    ;

    /** Çizgi çizen araçlar fırça boyutunu kullanır; kova ve çıkartma kullanmaz. */
    val usesBrushSize: Boolean
        get() = this != BUCKET && this != STICKER
}
