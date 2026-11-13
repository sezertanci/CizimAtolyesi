package dev.sezertanci.drawingworkshop.domain.geometry

import org.junit.Assert.assertEquals
import org.junit.Test

class FitCenterTest {

    @Test
    fun fitCenter_SameSize_FillsTarget() {
        assertEquals(ScaledBounds(0f, 0f, 800f, 600f), fitCenter(800, 600, 800, 600))
    }

    @Test
    fun fitCenter_WiderTarget_CentersHorizontally() {
        assertEquals(ScaledBounds(100f, 0f, 800f, 600f), fitCenter(400, 300, 1000, 600))
    }

    @Test
    fun fitCenter_TallerTarget_CentersVertically() {
        assertEquals(ScaledBounds(0f, 50f, 400f, 300f), fitCenter(800, 600, 400, 400))
    }

    @Test
    fun toPixelRect_FractionalBounds_RoundsEdges() {
        assertEquals(PixelRect(1, 2, 13, 7), ScaledBounds(0.6f, 2.4f, 12.2f, 4.5f).toPixelRect())
    }

    @Test
    fun fitLayer_NoContent_MatchesFitCenter() {
        assertEquals(fitCenter(800, 600, 1000, 600), fitLayer(800, 600, 1000, 600))
    }

    @Test
    fun fitLayer_WiderContent_AlignsContentAreas() {
        // 2:1 içerik, 400x400 katmanda (0,100)-(400,300) alanında; 800x200 tuvalde (200,0)-(600,200) alanında.
        assertEquals(ScaledBounds(200f, -100f, 400f, 400f), fitLayer(400, 400, 800, 200, 200, 100))
    }

    @Test
    fun fitLayer_SameSize_KeepsLayerInPlace() {
        assertEquals(ScaledBounds(0f, 0f, 400f, 300f), fitLayer(400, 300, 400, 300, 200, 100))
    }
}
