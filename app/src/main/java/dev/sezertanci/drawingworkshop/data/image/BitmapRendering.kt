package dev.sezertanci.drawingworkshop.data.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.roundToInt

private const val PNG_QUALITY = 100
private val scalePaint = Paint(Paint.FILTER_BITMAP_FLAG)

/**
 * Arka plan rengini ve boya katmanını tek bir görüntüde birleştirir. [maxSide] verilirse uzun kenar
 * bu değere indirilir (küçük resim için); oran korunur.
 */
fun renderFlattened(layer: Bitmap, backgroundColor: Int, maxSide: Int? = null): Bitmap {
    val scale = maxSide?.let { (it.toFloat() / max(layer.width, layer.height)).coerceAtMost(1f) } ?: 1f
    val width = (layer.width * scale).roundToInt().coerceAtLeast(1)
    val height = (layer.height * scale).roundToInt().coerceAtLeast(1)
    val result = createBitmap(width, height)
    Canvas(result).apply {
        drawColor(backgroundColor)
        scale(scale, scale)
        drawBitmap(layer, 0f, 0f, scalePaint)
    }
    return result
}

fun Bitmap.writePng(stream: OutputStream): Boolean = compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
