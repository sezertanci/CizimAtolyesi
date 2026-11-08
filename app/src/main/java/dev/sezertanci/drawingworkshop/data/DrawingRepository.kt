package dev.sezertanci.drawingworkshop.data

import android.graphics.Bitmap
import dev.sezertanci.drawingworkshop.domain.drawing.Drawing
import kotlinx.coroutines.flow.Flow

/**
 * Kaydedilecek resim içeriği. [layer] kaydedildikten sonra serbest bırakılır.
 * [templateId] resmin açıldığı boyama sayfasıdır; boş tuvalse `null`.
 */
class SaveRequest(
    val id: String,
    val layer: Bitmap,
    val backgroundColor: Int,
    val templateId: String?,
    val updatedAt: Long,
)

/** Açılan resmin kayıtlı içeriği. Boya katmanı dosyası okunamazsa [layer] `null` olur. */
class StoredDrawing(val backgroundColor: Int, val layer: Bitmap?, val templateId: String?)

interface DrawingRepository {
    /** Galerideki resimler, en son düzenlenen başta. */
    fun observeDrawings(): Flow<List<Drawing>>

    /** Çöp kutusundaki resimler, en son atılan başta. */
    fun observeTrash(): Flow<List<Drawing>>

    /** Resim hiç kaydedilmemişse `null` döner. */
    suspend fun load(id: String): StoredDrawing?

    suspend fun save(request: SaveRequest)

    suspend fun moveToTrash(id: String)

    suspend fun restore(id: String)

    /** Çöp kutusunda 24 saati dolan resimleri dosyalarıyla birlikte kalıcı olarak siler. */
    suspend fun purgeExpiredTrash()
}
