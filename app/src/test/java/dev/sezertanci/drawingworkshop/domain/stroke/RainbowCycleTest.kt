package dev.sezertanci.drawingworkshop.domain.stroke

import org.junit.Assert.assertEquals
import org.junit.Test

class RainbowCycleTest {
    private val cycle = RainbowCycle(CYCLE_LENGTH)

    @Test
    fun stepAt_StrokeStart_IsRed() {
        assertEquals(RED, RainbowCycle.colorOf(cycle.stepAt(0f)))
    }

    @Test
    fun stepAt_ThirdOfCycle_IsGreen() {
        assertEquals(GREEN, RainbowCycle.colorOf(cycle.stepAt(CYCLE_LENGTH / 3f)))
    }

    @Test
    fun stepAt_TwoThirdsOfCycle_IsBlue() {
        assertEquals(BLUE, RainbowCycle.colorOf(cycle.stepAt(CYCLE_LENGTH * 2f / 3f)))
    }

    @Test
    fun stepAt_FullCycle_ReturnsToRed() {
        assertEquals(0, cycle.stepAt(CYCLE_LENGTH))
        assertEquals(cycle.stepAt(10f), cycle.stepAt(CYCLE_LENGTH + 10f))
    }

    @Test
    fun stepAt_SmallMove_StaysOnSameStep() {
        assertEquals(cycle.stepAt(0f), cycle.stepAt(CYCLE_LENGTH / RainbowCycle.STEPS / 2f))
    }

    @Test
    fun stepAt_AnyDistance_StaysInRange() {
        val steps = (0..1000).map { cycle.stepAt(it * 7.3f) }

        assertEquals(true, steps.all { it in 0 until RainbowCycle.STEPS })
    }

    private companion object {
        const val CYCLE_LENGTH = 480f
        const val RED = 0xFFFF0000.toInt()
        const val GREEN = 0xFF00FF00.toInt()
        const val BLUE = 0xFF0000FF.toInt()
    }
}
