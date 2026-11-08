package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.abs

data class StrokePoint(val x: Float, val y: Float)

/** İkinci dereceden bir eğri parçası: [control] noktasından geçerek [end] noktasına uzanır. */
data class QuadSegment(val control: StrokePoint, val end: StrokePoint)

/**
 * Dokunma noktalarını yumuşak eğrilere çevirir.
 *
 * Ardışık iki noktanın orta noktaları eğri uçları, gerçek noktalar ise kontrol noktaları olarak
 * kullanılır. Bu yöntem köşeli çizgiler yerine akıcı çizgiler üretir. [minDistance] değerinden
 * yakın noktalar atlanır; böylece parmak titremesi gereksiz parça üretmez.
 */
class StrokeSmoother(private val minDistance: Float) {
    private var last: StrokePoint? = null
    private var lastMid: StrokePoint? = null

    fun start(point: StrokePoint) {
        last = point
        lastMid = point
    }

    /** Yeni noktayı ekler; yeterince uzaksa çizilecek parçayı döndürür. */
    fun add(point: StrokePoint): QuadSegment? {
        val previous = last ?: return null
        if (abs(point.x - previous.x) < minDistance && abs(point.y - previous.y) < minDistance) return null
        val mid = StrokePoint((previous.x + point.x) / 2f, (previous.y + point.y) / 2f)
        last = point
        lastMid = mid
        return QuadSegment(control = previous, end = mid)
    }

    /** Çizginin son orta noktadan parmağın kalktığı noktaya kadar kapanan parçasını döndürür. */
    fun finish(): QuadSegment? {
        val previous = last ?: return null
        val mid = lastMid ?: return null
        last = null
        lastMid = null
        if (previous == mid) return null
        return QuadSegment(control = previous, end = previous)
    }
}
