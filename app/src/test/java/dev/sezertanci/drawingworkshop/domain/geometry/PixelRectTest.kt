package dev.sezertanci.drawingworkshop.domain.geometry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PixelRectTest {

    @Test
    fun contains_PointOnRightEdge_ReturnsFalse() {
        assertFalse(PixelRect(0, 0, 10, 10).contains(10, 5))
    }

    @Test
    fun contains_PointOnLeftEdge_ReturnsTrue() {
        assertTrue(PixelRect(0, 0, 10, 10).contains(0, 5))
    }

    @Test
    fun intersects_TouchingEdges_ReturnsFalse() {
        assertFalse(PixelRect(0, 0, 10, 10).intersects(PixelRect(10, 0, 20, 10)))
    }

    @Test
    fun mergeOverlapping_SeparateRects_KeepsThem() {
        val rects = listOf(PixelRect(0, 0, 10, 10), PixelRect(20, 20, 30, 30))

        assertEquals(rects, rects.mergeOverlapping())
    }

    @Test
    fun mergeOverlapping_OverlappingRects_ReturnsUnion() {
        val rects = listOf(PixelRect(0, 0, 10, 10), PixelRect(5, 5, 15, 15))

        assertEquals(listOf(PixelRect(0, 0, 15, 15)), rects.mergeOverlapping())
    }

    @Test
    fun mergeOverlapping_UnionReachesEarlierRect_MergesAllThree() {
        val rects = listOf(PixelRect(30, 0, 40, 10), PixelRect(0, 0, 10, 10), PixelRect(5, 5, 35, 8))

        assertEquals(listOf(PixelRect(0, 0, 40, 10)), rects.mergeOverlapping())
    }
}
