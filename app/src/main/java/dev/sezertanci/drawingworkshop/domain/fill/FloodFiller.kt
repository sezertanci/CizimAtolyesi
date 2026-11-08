package dev.sezertanci.drawingworkshop.domain.fill

/** Doldurmanın etkilediği dikdörtgen. [right] ve [bottom] dahil değildir. */
data class FillBounds(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
}

/** Boya kovası algoritması. Piksel dizisi ARGB biçimindedir ve yerinde değiştirilir. */
interface FloodFiller {
    /**
     * Değişen alanı döndürür; hiçbir piksel değişmediyse `null` döner. [walls] verilirse `true` olan
     * pikseller duvar sayılır: dolgu bunları boyamaz ve öteye geçmez (boyama sayfasının çizgileri).
     */
    fun fill(
        pixels: IntArray,
        width: Int,
        height: Int,
        startX: Int,
        startY: Int,
        color: Int,
        walls: BooleanArray? = null,
    ): FillBounds?
}
