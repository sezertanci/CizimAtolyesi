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
}
