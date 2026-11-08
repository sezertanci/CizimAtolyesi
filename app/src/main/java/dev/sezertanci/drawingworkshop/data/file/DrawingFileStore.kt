package dev.sezertanci.drawingworkshop.data.file

import android.graphics.Bitmap

/** Resimlerin görüntü dosyalarını (boya katmanı ve küçük resim) saklar. */
interface DrawingFileStore {
    /** Boya katmanını ve ondan üretilen küçük resmi yazar. Başarısız olursa `false` döner. */
    suspend fun write(id: String, layer: Bitmap, backgroundColor: Int): Boolean

    suspend fun readLayer(id: String): Bitmap?

    suspend fun readThumbnail(id: String): Bitmap?

    suspend fun delete(id: String)
}
