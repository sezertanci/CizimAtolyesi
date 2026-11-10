package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.ceil
import kotlin.math.hypot

/**
 * [start] noktasından başlayan eğri parçasını, her biri en fazla [maxPieceLength] uzunluğunda kısa düz
 * parçalara böler ve bu parçaların bitiş noktalarını sırayla döndürür. Son nokta her zaman eğrinin
 * bitiş noktasıdır.
 */
fun QuadSegment.divide(start: StrokePoint, maxPieceLength: Float): List<StrokePoint> {
    // Kontrol noktası üzerinden geçen yol, eğrinin uzunluğundan hiçbir zaman kısa değildir.
    val outline = distance(start, control) + distance(control, end)
    val count = ceil(outline / maxPieceLength).toInt().coerceAtLeast(1)
    return (1..count).map { pointAt(start, it.toFloat() / count) }
}

private fun QuadSegment.pointAt(start: StrokePoint, t: Float): StrokePoint {
    val rest = 1f - t
    val a = rest * rest
    val b = 2f * rest * t
    val c = t * t
    return StrokePoint(
        a * start.x + b * control.x + c * end.x,
        a * start.y + b * control.y + c * end.y,
    )
}

private fun distance(from: StrokePoint, to: StrokePoint): Float = hypot(to.x - from.x, to.y - from.y)
