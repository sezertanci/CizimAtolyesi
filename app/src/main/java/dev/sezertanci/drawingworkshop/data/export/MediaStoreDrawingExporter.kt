package dev.sezertanci.drawingworkshop.data.export

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sezertanci.drawingworkshop.data.image.renderFlattened
import dev.sezertanci.drawingworkshop.data.image.writePng
import dev.sezertanci.drawingworkshop.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Clock
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * PNG dosyasını MediaStore ile `Pictures/Çizim Atölyesi` klasörüne yazar. Uygulama kendi eklediği
 * dosyaya yazdığı için depolama izni gerekmez. Dosya yazılırken "bekliyor" olarak işaretlenir;
 * yazma başarısız olursa yarım dosya silinir.
 */
class MediaStoreDrawingExporter @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val clock: Clock,
) : DrawingExporter {

    override suspend fun export(layer: Bitmap, backgroundColor: Int): Boolean = withContext(ioDispatcher) {
        val image = try {
            renderFlattened(layer, backgroundColor)
        } finally {
            layer.recycle()
        }
        try {
            insert(context.contentResolver, image)
        } finally {
            image.recycle()
        }
    }

    private fun insert(resolver: ContentResolver, image: Bitmap): Boolean {
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, pendingValues()) ?: return false
        var published = false
        try {
            val written = resolver.openOutputStream(uri)?.use { image.writePng(it) } == true
            if (written) {
                resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
                published = true
            }
        } catch (e: IOException) {
            Log.e(TAG, "Resim galeriye aktarılamadı", e)
        } finally {
            if (!published) deleteQuietly(resolver, uri)
        }
        return published
    }

    private fun pendingValues() = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName())
        put(MediaStore.Images.Media.MIME_TYPE, MIME_PNG)
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + ALBUM)
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    private fun fileName(): String =
        FILE_PREFIX + SimpleDateFormat(FILE_DATE_PATTERN, Locale.ROOT).format(Date(clock.millis())) + FILE_EXTENSION

    private fun deleteQuietly(resolver: ContentResolver, uri: Uri) {
        runCatching { resolver.delete(uri, null, null) }
    }

    private companion object {
        const val TAG = "DrawingExporter"
        const val ALBUM = "Çizim Atölyesi"
        const val MIME_PNG = "image/png"
        const val FILE_PREFIX = "Çizim_"
        const val FILE_DATE_PATTERN = "yyyyMMdd_HHmmss"
        const val FILE_EXTENSION = ".png"
    }
}
