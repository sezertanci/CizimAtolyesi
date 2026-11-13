package dev.sezertanci.drawingworkshop.data.coloring

import android.graphics.Canvas
import android.util.Size
import dev.sezertanci.drawingworkshop.domain.geometry.PixelRect

/** Boyama sayfasının çizgilerini bir tuvale çizer. */
interface ColoringPageRenderer {
    /**
     * Çizgileri [width] x [height] alanına oranı koruyarak ortalanmış çizer. Kimlik tanınmıyorsa
     * hiçbir şey çizilmez. Tuval, kayıt ve dışa aktarmada aynı yerleşim kullanılır.
     */
    fun draw(templateId: String, canvas: Canvas, width: Int, height: Int)

    /** Çizimin [width] x [height] tuvalinde kapladığı alan; [draw] ile aynı yerleşim. Kimlik tanınmıyorsa `null`. */
    fun artBounds(templateId: String, width: Int, height: Int): PixelRect?

    /** Çizimin kendi boyutu; yalnızca en-boy oranı anlamlıdır. Kimlik tanınmıyorsa `null`. */
    fun artSize(templateId: String): Size?
}
