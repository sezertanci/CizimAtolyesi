package dev.sezertanci.drawingworkshop.data.coloring

import android.graphics.Canvas

/** Boyama sayfasının çizgilerini bir tuvale çizer. */
interface ColoringPageRenderer {
    /**
     * Çizgileri [width] x [height] alanına oranı koruyarak ortalanmış çizer. Kimlik tanınmıyorsa
     * hiçbir şey çizilmez. Tuval, kayıt ve dışa aktarmada aynı yerleşim kullanılır.
     */
    fun draw(templateId: String, canvas: Canvas, width: Int, height: Int)
}
