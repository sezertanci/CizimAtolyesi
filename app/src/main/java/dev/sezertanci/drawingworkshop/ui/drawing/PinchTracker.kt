package dev.sezertanci.drawingworkshop.ui.drawing

import android.view.MotionEvent
import kotlin.math.hypot

/** İki parmak hareketinin bir adımı: odak noktası, büyütme katsayısı ve kaydırma miktarı (ekran pikseli). */
data class PinchStep(val focusX: Float, val focusY: Float, val scaleFactor: Float, val panX: Float, val panY: Float)

/**
 * İlk iki parmağın orta noktasını ve aralarındaki mesafeyi izler; her harekette bir önceki konuma
 * göre ne kadar büyütüldüğünü ve kaydırıldığını hesaplar. Ekranda iki parmak yoksa hareket üretmez.
 */
class PinchTracker {
    private var centerX = 0f
    private var centerY = 0f
    private var span = 0f
    private var isTracking = false

    /** Parmak eklenince veya kalkınca başlangıç konumunu yeniler. [excludeIndex] kalkmakta olan parmaktır. */
    fun reset(event: MotionEvent, excludeIndex: Int = NO_INDEX) {
        val indices = (0 until event.pointerCount).filter { it != excludeIndex }.take(PINCH_POINTERS)
        isTracking = indices.size == PINCH_POINTERS
        if (isTracking) measure(event, indices[0], indices[1])
    }

    fun update(event: MotionEvent): PinchStep? {
        if (!isTracking || event.pointerCount < PINCH_POINTERS) return null
        val previousX = centerX
        val previousY = centerY
        val previousSpan = span
        measure(event, 0, 1)
        val scaleFactor = if (previousSpan > MIN_SPAN && span > MIN_SPAN) span / previousSpan else 1f
        return PinchStep(centerX, centerY, scaleFactor, centerX - previousX, centerY - previousY)
    }

    private fun measure(event: MotionEvent, first: Int, second: Int) {
        val x1 = event.getX(first)
        val y1 = event.getY(first)
        val x2 = event.getX(second)
        val y2 = event.getY(second)
        centerX = (x1 + x2) / 2f
        centerY = (y1 + y2) / 2f
        span = hypot(x2 - x1, y2 - y1)
    }

    private companion object {
        const val PINCH_POINTERS = 2
        const val NO_INDEX = -1
        const val MIN_SPAN = 1f
    }
}
