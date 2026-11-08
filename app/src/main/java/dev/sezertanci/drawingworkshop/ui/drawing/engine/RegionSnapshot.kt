package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Bitmap

/** Boya katmanının bir bölgesinin kopyası. Geri al / yinele için kullanılır. */
class RegionSnapshot(val bitmap: Bitmap, val left: Int, val top: Int) {
    fun release() {
        bitmap.recycle()
    }
}
