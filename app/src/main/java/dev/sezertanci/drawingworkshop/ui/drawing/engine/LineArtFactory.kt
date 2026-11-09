package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.util.Size
import androidx.core.graphics.createBitmap
import dev.sezertanci.drawingworkshop.data.coloring.ColoringPageRenderer
import dev.sezertanci.drawingworkshop.di.DefaultDispatcher
import dev.sezertanci.drawingworkshop.domain.fill.LineWalls
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Boyama sayfasının çizgilerini tuval boyutunda hazırlar. Ağır iş arka planda yapılır. */
class LineArtFactory @Inject constructor(
    private val pageRenderer: ColoringPageRenderer,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) {
    /** Sayfa çiziminin kendi boyutu; sayfa tanınmıyorsa `null`. */
    fun artSize(templateId: String): Size? = pageRenderer.artSize(templateId)

    suspend fun create(templateId: String, width: Int, height: Int): LineArt = withContext(dispatcher) {
        val bitmap = createBitmap(width, height)
        pageRenderer.draw(templateId, Canvas(bitmap), width, height)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        LineArt(bitmap, LineWalls.fromPixels(pixels))
    }
}
