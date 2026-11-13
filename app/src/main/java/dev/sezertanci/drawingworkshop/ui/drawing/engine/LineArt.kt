package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Bitmap
import dev.sezertanci.drawingworkshop.domain.geometry.PixelRect

/**
 * Boyama sayfasının tuval boyutundaki çizgi görüntüsü, boya kovası için duvar haritası ve çizimin
 * tuvalde kapladığı alan ([artBounds]). Kova bu alanın kenarından taşmaz.
 */
class LineArt(val bitmap: Bitmap, val walls: BooleanArray, val artBounds: PixelRect?) {
    fun matches(width: Int, height: Int): Boolean = bitmap.width == width && bitmap.height == height

    fun release() = bitmap.recycle()
}
