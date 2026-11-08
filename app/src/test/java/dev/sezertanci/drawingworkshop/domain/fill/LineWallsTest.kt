package dev.sezertanci.drawingworkshop.domain.fill

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class LineWallsTest {

    @Test
    fun fromPixels_MixedAlpha_MarksOnlyDarkLinePixels() {
        val pixels = intArrayOf(TRANSPARENT, SOFT_EDGE, WALL_EDGE, BLACK)

        val walls = LineWalls.fromPixels(pixels)

        assertArrayEquals(booleanArrayOf(false, false, true, true), walls)
    }

    private companion object {
        const val TRANSPARENT = 0
        const val SOFT_EDGE = (LineWalls.WALL_ALPHA - 1) shl 24
        const val WALL_EDGE = LineWalls.WALL_ALPHA shl 24
        const val BLACK = 0xFF000000.toInt()
    }
}
