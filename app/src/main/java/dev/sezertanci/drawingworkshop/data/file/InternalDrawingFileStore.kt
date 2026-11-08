package dev.sezertanci.drawingworkshop.data.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sezertanci.drawingworkshop.data.image.DrawingFlattener
import dev.sezertanci.drawingworkshop.data.image.writePng
import dev.sezertanci.drawingworkshop.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

/**
 * Dosyaları uygulamanın iç deposunda, her resim için ayrı bir klasörde tutar. Başka uygulamalar
 * bu dosyalara erişemez. Yazma önce geçici dosyaya yapılır, sonra yeniden adlandırılır; böylece
 * yarıda kesilen bir kayıt eski dosyayı bozmaz.
 */
class InternalDrawingFileStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val flattener: DrawingFlattener,
) : DrawingFileStore {

    private val root: File get() = File(context.filesDir, ROOT_DIR)

    override suspend fun write(id: String, layer: Bitmap, backgroundColor: Int, templateId: String?): Boolean =
        withContext(ioDispatcher) {
            val dir = directory(id).apply { mkdirs() }
            val thumbnail = flattener.flatten(layer, backgroundColor, templateId, THUMBNAIL_MAX_SIDE)
            try {
                writeAtomically(layer, File(dir, LAYER_FILE)) && writeAtomically(thumbnail, File(dir, THUMBNAIL_FILE))
            } finally {
                thumbnail.recycle()
            }
        }

    override suspend fun readLayer(id: String): Bitmap? = decode(File(directory(id), LAYER_FILE))

    override suspend fun readThumbnail(id: String): Bitmap? = decode(File(directory(id), THUMBNAIL_FILE))

    override suspend fun delete(id: String) {
        withContext(ioDispatcher) { directory(id).deleteRecursively() }
    }

    private suspend fun decode(file: File): Bitmap? = withContext(ioDispatcher) {
        if (file.exists()) BitmapFactory.decodeFile(file.path) else null
    }

    private fun writeAtomically(bitmap: Bitmap, target: File): Boolean {
        val temp = File(target.parentFile, target.name + TEMP_SUFFIX)
        return try {
            val written = temp.outputStream().use { bitmap.writePng(it) }
            written && temp.renameTo(target)
        } catch (e: IOException) {
            Log.e(TAG, "Dosya yazılamadı: ${target.name}", e)
            false
        } finally {
            temp.delete()
        }
    }

    /** Kimlik dosya yoluna girdiği için yalnızca UUID biçimi kabul edilir; böylece klasör dışına çıkılamaz. */
    private fun directory(id: String): File {
        require(runCatching { UUID.fromString(id) }.isSuccess) { "Geçersiz resim kimliği" }
        return File(root, id)
    }

    private companion object {
        const val TAG = "DrawingFileStore"
        const val ROOT_DIR = "drawings"
        const val LAYER_FILE = "layer.png"
        const val THUMBNAIL_FILE = "thumbnail.png"
        const val TEMP_SUFFIX = ".tmp"
        const val THUMBNAIL_MAX_SIDE = 480
    }
}
