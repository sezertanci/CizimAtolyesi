package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Bitmap

/** Boya katmanının bir bölgesinin kopyası. Geri al / yinele için kullanılır. */
class RegionSnapshot(val bitmap: Bitmap, val left: Int, val top: Int) {
    fun release() {
        bitmap.recycle()
    }
}

/**
 * Tek bir işlemin geri al kaydı. Simetrik çizgide her kopyanın bölgesi ayrı tutulur; böylece köşeye
 * çizilen bir çizgi için bütün tuvalin kopyası alınmaz. Bölgeler birbiriyle kesişmez.
 */
class LayerChange(val snapshots: List<RegionSnapshot>) {
    fun release() {
        snapshots.forEach(RegionSnapshot::release)
    }
}
