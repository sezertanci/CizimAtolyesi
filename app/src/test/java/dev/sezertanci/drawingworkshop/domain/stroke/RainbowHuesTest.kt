package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RainbowHuesTest {
    @Test
    fun colorOf_PaletteStops_ReturnPaletteColors() {
        val palette = RainbowHues.PALETTE
        val share = FULL / palette.size

        palette.forEachIndexed { index, rgb ->
            assertEquals(OPAQUE or rgb, RainbowHues.colorOf(index * share))
        }
        assertEquals(OPAQUE or palette[0], RainbowHues.colorOf(FULL))
    }

    @Test
    fun colorOf_InsideHoldShare_KeepsPureColor() {
        val share = FULL / RainbowHues.PALETTE.size

        val color = RainbowHues.colorOf(share * RainbowHues.COLOR_HOLD_SHARE * 0.9f)

        assertEquals(OPAQUE or RainbowHues.PALETTE[0], color)
    }

    @Test
    fun colorOf_MiddleOfBlend_MixesNeighbours() {
        val share = FULL / RainbowHues.PALETTE.size
        val hold = RainbowHues.COLOR_HOLD_SHARE

        val color = RainbowHues.colorOf(share * (hold + (1f - hold) / 2f))

        // Kırmızı (FF0000) ile turuncu (FF8C00) arası: kırmızı tam, yeşil kanal ikisinin ortasında.
        assertEquals(0xFF, (color shr 16) and 0xFF)
        assertEquals(0x46, (color shr 8) and 0xFF)
        assertEquals(0, color and 0xFF)
    }

    @Test
    fun hueAt_DifferentStrokes_StartWithDifferentHues() {
        val starts = (1..20).map { seed -> RainbowHues(LEG, Random(seed)).hueAt(0f) }

        assertTrue(starts.distinct().size > 1)
    }

    @Test
    fun hueAt_SameSeed_RepeatsSameColors() {
        val first = RainbowHues(LEG, Random(SEED))
        val second = RainbowHues(LEG, Random(SEED))

        assertEquals(first.hueAt(LEG * 2.5f), second.hueAt(LEG * 2.5f), 0f)
    }

    @Test
    fun hueAt_EndOfLeg_ChangesByAllowedTurn() {
        val hues = RainbowHues(LEG, Random(SEED))

        val turn = hueDistance(hues.hueAt(0f), hues.hueAt(LEG))

        assertTrue(turn >= min(RainbowHues.MIN_TURN_DEGREES, FULL - RainbowHues.MAX_TURN_DEGREES) - TOLERANCE)
        assertNotEquals(hues.hueAt(0f), hues.hueAt(LEG), TOLERANCE)
    }

    @Test
    fun hueAt_SmallMoves_ChangeHueGradually() {
        val hues = RainbowHues(LEG, Random(SEED))
        val samples = (0..LEG_COUNT * STEPS_PER_LEG).map { hues.hueAt(it * LEG / STEPS_PER_LEG) }

        val largestJump = samples.zipWithNext { a, b -> hueDistance(a, b) }.max()

        // Tek kademede, en büyük dönüşün düz dağılımla alacağı payın iki katından fazla değişmemeli.
        assertTrue(largestJump <= RainbowHues.MAX_TURN_DEGREES / STEPS_PER_LEG * 2f)
    }

    @Test
    fun hueAt_AnyDistance_StaysInRange() {
        val hues = RainbowHues(LEG, Random(SEED))

        val all = (0..1000).map { hues.hueAt(it * 7.3f) }

        assertTrue(all.all { it >= 0f && it < FULL })
    }

    private fun hueDistance(a: Float, b: Float): Float {
        val diff = abs(a - b).mod(FULL)
        return min(diff, FULL - diff)
    }

    private companion object {
        const val LEG = 160f
        const val SEED = 7
        const val LEG_COUNT = 10
        const val STEPS_PER_LEG = 40
        const val TOLERANCE = 0.01f
        const val FULL = RainbowHues.FULL_TURN_DEGREES
        const val OPAQUE = 0xFF shl 24
    }
}
