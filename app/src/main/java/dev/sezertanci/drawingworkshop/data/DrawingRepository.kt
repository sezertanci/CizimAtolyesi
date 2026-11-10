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

/** Galerinin şu ana kadar yüklenen kısmı; [hasMore] ise sırada başka resim olduğunu gösterir. */
data class DrawingPage(val drawings: List<Drawing>, val hasMore: Boolean)

interface DrawingRepository {
    /** Galerideki ilk [limit] resim, en son düzenlenen başta. */
    fun observeDrawings(limit: Int): Flow<DrawingPage>

    /** Çöp kutusundaki resimler, en son atılan başta. */
    fun observeTrash(): Flow<List<Drawing>>

    /** Resim hiç kaydedilmemişse `null` döner. */
    suspend fun load(id: String): StoredDrawing?

    suspend fun save(request: SaveRequest)

    suspend fun moveToTrash(id: String)

    suspend fun restore(id: String)

    /** Çöp kutusundaki resmi dosyalarıyla birlikte hemen ve kalıcı olarak siler. */
    suspend fun deleteForever(id: String)

    /** Çöp kutusundaki bütün resimleri dosyalarıyla birlikte kalıcı olarak siler. */
    suspend fun emptyTrash()

    /** Çöp kutusunda 24 saati dolan resimleri dosyalarıyla birlikte kalıcı olarak siler. */
    suspend fun purgeExpiredTrash()
}
