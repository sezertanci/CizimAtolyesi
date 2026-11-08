package dev.sezertanci.drawingworkshop.data.export

import android.graphics.Bitmap

/** Resmi cihazın fotoğraf galerisine aktarır. */
interface DrawingExporter {
    /** [layer] işlem bitince serbest bırakılır. Başarılıysa `true` döner. */
    suspend fun export(layer: Bitmap, backgroundColor: Int): Boolean
}
