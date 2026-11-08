package dev.sezertanci.drawingworkshop.domain.stroke

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StrokeSmootherTest {
    private val smoother = StrokeSmoother(MIN_DISTANCE)

    @Test
    fun add_PointCloserThanMinDistance_ReturnsNull() {
        smoother.start(StrokePoint(0f, 0f))

        val segment = smoother.add(StrokePoint(1f, 1f))

        assertNull(segment)
    }

    @Test
    fun add_FarPoint_ReturnsSegmentEndingAtMidpoint() {
        smoother.start(StrokePoint(0f, 0f))

        val segment = smoother.add(StrokePoint(10f, 20f))

        assertEquals(QuadSegment(control = StrokePoint(0f, 0f), end = StrokePoint(5f, 10f)), segment)
    }

    @Test
    fun finish_AfterMove_ClosesAtLastPoint() {
        smoother.start(StrokePoint(0f, 0f))
        smoother.add(StrokePoint(10f, 0f))

        val segment = smoother.finish()

        assertEquals(QuadSegment(StrokePoint(10f, 0f), StrokePoint(10f, 0f)), segment)
    }

    @Test
    fun finish_OnlyStarted_ReturnsNull() {
        smoother.start(StrokePoint(3f, 3f))

        assertNull(smoother.finish())
    }

    @Test
    fun add_WithoutStart_ReturnsNull() {
        assertNull(smoother.add(StrokePoint(50f, 50f)))
    }

    private companion object {
        const val MIN_DISTANCE = 2f
    }
}
