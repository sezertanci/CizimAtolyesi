package dev.sezertanci.drawingworkshop.domain.fill

import dev.sezertanci.drawingworkshop.domain.geometry.PixelRect

/** Boya kovası algoritması. Piksel dizisi ARGB biçimindedir ve yerinde değiştirilir. */
interface FloodFiller {
    /**
     * Değişen alanı döndürür; hiçbir piksel değişmediyse `null` döner. [walls] verilirse `true` olan
     * pikseller duvar sayılır: dolgu bunları boyamaz ve öteye geçmez (boyama sayfasının çizgileri).
     * [boundary] verilirse kenarı da duvar gibi davranır: dolgu, başladığı noktanın bulunduğu tarafta
     * (içinde ya da dışında) kalır. Böylece kenara açılan bölgeler çizimin dışına taşmaz.
     */
    fun fill(
        pixels: IntArray,
        width: Int,
        height: Int,
        startX: Int,
        startY: Int,
        color: Int,
        walls: BooleanArray? = null,
        boundary: PixelRect? = null,
    ): PixelRect?
}
