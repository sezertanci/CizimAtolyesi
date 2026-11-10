package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.hypot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuadDividerTest {
    @Test
    fun divide_ShortSegment_ReturnsOnlyEnd() {
        val segment = QuadSegment(StrokePoint(1f, 0f), StrokePoint(2f, 0f))

        val points = segment.divide(ORIGIN, MAX_PIECE)

        assertEquals(listOf(StrokePoint(2f, 0f)), points)
    }

    @Test
    fun divide_LongCurve_EndsAtSegmentEnd() {
        val segment = QuadSegment(StrokePoint(50f, 80f), StrokePoint(100f, 0f))

        val points = segment.divide(ORIGIN, MAX_PIECE)

        assertEquals(segment.end, points.last())
    }

    @Test
    fun divide_LongCurve_KeepsEveryPieceShort() {
        val segment = QuadSegment(StrokePoint(50f, 80f), StrokePoint(100f, 0f))

        val points = listOf(ORIGIN) + segment.divide(ORIGIN, MAX_PIECE)

        val longest = points.zipWithNext { a, b -> hypot(b.x - a.x, b.y - a.y) }.max()
        assertTrue(longest <= MAX_PIECE)
    }

    @Test
    fun divide_StraightLine_SplitsEvenly() {
        val segment = QuadSegment(StrokePoint(10f, 0f), StrokePoint(20f, 0f))

        val points = segment.divide(ORIGIN, MAX_PIECE)

        assertEquals(listOf(5f, 10f, 15f, 20f), points.map { it.x })
    }

    private companion object {
        const val MAX_PIECE = 5f
        val ORIGIN = StrokePoint(0f, 0f)
    }
}
