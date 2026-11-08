package dev.sezertanci.drawingworkshop.ui.drawing.engine

/** Ekranın piksel yoğunluğu (1 dp kaç piksel). dp cinsinden boyutları piksele çevirmek için kullanılır. */
data class ScreenDensity(val value: Float) {
    fun toPx(dp: Float): Float = dp * value
}
