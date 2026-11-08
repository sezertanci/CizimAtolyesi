package dev.sezertanci.drawingworkshop.data.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import dev.sezertanci.drawingworkshop.data.coloring.ColoringPageRenderer
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Resmin katmanlarını tek bir görüntüde birleştirir: arka plan rengi, boya katmanı ve varsa boyama
 * sayfasının çizgileri (en üstte). Küçük resim ve dışa aktarma bunu kullanır.
 */
class DrawingFlattener @Inject constructor(private val pageRenderer: ColoringPageRenderer) {
    private val scalePaint = Paint(Paint.FILTER_BITMAP_FLAG)

    /** [maxSide] verilirse uzun kenar bu değere indirilir (küçük resim için); oran korunur. */
    fun flatten(layer: Bitmap, backgroundColor: Int, templateId: String?, maxSide: Int? = null): Bitmap {
        val scale = maxSide?.let { (it.toFloat() / max(layer.width, layer.height)).coerceAtMost(1f) } ?: 1f
        val width = (layer.width * scale).roundToInt().coerceAtLeast(1)
        val height = (layer.height * scale).roundToInt().coerceAtLeast(1)
        val result = createBitmap(width, height)
        Canvas(result).apply {
            drawColor(backgroundColor)
            scale(scale, scale)
            drawBitmap(layer, 0f, 0f, scalePaint)
            templateId?.let { pageRenderer.draw(it, this, layer.width, layer.height) }
        }
        return result
    }
}
