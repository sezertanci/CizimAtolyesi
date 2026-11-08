package dev.sezertanci.drawingworkshop.ui.gallery

import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import dev.sezertanci.drawingworkshop.data.file.DrawingFileStore
import dev.sezertanci.drawingworkshop.domain.drawing.Drawing
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Galerideki küçük resimleri arka planda okur ve bellekte tutar. Anahtar güncelleme zamanını da
 * içerdiği için düzenlenen resmin yeni hali okunur.
 */
@Singleton
class ThumbnailLoader @Inject constructor(
    private val fileStore: DrawingFileStore,
) {
    private val cache = object : LruCache<String, ImageBitmap>(CACHE_BYTES) {
        override fun sizeOf(key: String, value: ImageBitmap): Int = value.width * value.height * BYTES_PER_PIXEL
    }

    suspend fun load(drawing: Drawing): ImageBitmap? {
        val key = "${drawing.id}:${drawing.updatedAt}"
        cache.get(key)?.let { return it }
        return fileStore.readThumbnail(drawing.id)?.asImageBitmap()?.also { cache.put(key, it) }
    }

    private companion object {
        const val CACHE_BYTES = 24 * 1024 * 1024
        const val BYTES_PER_PIXEL = 4
    }
}
