package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Bitmap

/** Boyama sayfasının tuval boyutundaki çizgi görüntüsü ve boya kovası için duvar haritası. */
class LineArt(val bitmap: Bitmap, val walls: BooleanArray) {
    fun matches(width: Int, height: Int): Boolean = bitmap.width == width && bitmap.height == height

    fun release() = bitmap.recycle()
}
