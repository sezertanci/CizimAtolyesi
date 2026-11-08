package dev.sezertanci.drawingworkshop.domain.fill

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanlineFloodFillerTest {
    private val filler = ScanlineFloodFiller()

    @Test
    fun fill_InsideClosedSquare_FillsOnlyInside() {
        // 7x7 tuval; 1..5 arasında siyah çerçeveli kare, içi 2..4.
        val pixels = canvas { x, y -> if (x in 1..5 && y in 1..5 && (x == 1 || x == 5 || y == 1 || y == 5)) BLACK else WHITE }

        val bounds = filler.fill(pixels, SIZE, SIZE, 3, 3, RED)

        assertEquals(FillBounds(2, 2, 5, 5), bounds)
        assertEquals(RED, pixels[3 * SIZE + 3])
        assertEquals(BLACK, pixels[1 * SIZE + 1])
        assertEquals(WHITE, pixels[0])
    }

    @Test
    fun fill_StartOutsideCanvas_ReturnsNull() {
        val pixels = canvas { _, _ -> WHITE }

        assertNull(filler.fill(pixels, SIZE, SIZE, SIZE, 0, RED))
        assertNull(filler.fill(pixels, SIZE, SIZE, -1, 0, RED))
    }

    @Test
    fun fill_SameColor_ReturnsNullAndKeepsPixels() {
        val pixels = canvas { _, _ -> RED }

        val bounds = filler.fill(pixels, SIZE, SIZE, 0, 0, RED)

        assertNull(bounds)
        assertTrue(pixels.all { it == RED })
    }

    @Test
    fun fill_TransparentCanvas_FillsEverything() {
        val pixels = canvas { _, _ -> TRANSPARENT }

        val bounds = filler.fill(pixels, SIZE, SIZE, 0, 0, RED)

        assertEquals(FillBounds(0, 0, SIZE, SIZE), bounds)
        assertTrue(pixels.all { it == RED })
    }

    @Test
    fun fill_TranslucentEdgeNextToRegion_BlendsEdge() {
        // Sol yarı saydam, orta sütun yarı saydam siyah kenar, sağ opak siyah.
        val pixels = canvas { x, _ -> if (x < 3) TRANSPARENT else if (x == 3) HALF_BLACK else BLACK }

        filler.fill(pixels, SIZE, SIZE, 0, 0, RED)

        val edge = pixels[3]
        assertEquals(ColorMath.MAX_CHANNEL, ColorMath.alpha(edge))
        assertTrue("Kenar ne kırmızı ne siyah olmalı", edge != RED && edge != BLACK)
        assertEquals(BLACK, pixels[4])
    }

    @Test
    fun fill_WallColumn_StopsAtWallAndLeavesItUnpainted() {
        val pixels = canvas { _, _ -> TRANSPARENT }
        val walls = BooleanArray(SIZE * SIZE) { it % SIZE == WALL_X }

        val bounds = filler.fill(pixels, SIZE, SIZE, 0, 0, RED, walls)

        assertEquals(FillBounds(0, 0, WALL_X, SIZE), bounds)
        assertEquals(RED, pixels[WALL_X - 1])
        assertEquals(TRANSPARENT, pixels[WALL_X])
        assertEquals(TRANSPARENT, pixels[WALL_X + 1])
    }

    @Test
    fun fill_StartOnWall_ReturnsNull() {
        val pixels = canvas { _, _ -> TRANSPARENT }
        val walls = BooleanArray(SIZE * SIZE) { it % SIZE == WALL_X }

        assertNull(filler.fill(pixels, SIZE, SIZE, WALL_X, 0, RED, walls))
        assertTrue(pixels.all { it == TRANSPARENT })
    }

    @Test
    fun sourceOver_OpaqueSource_ReturnsSource() {
        assertEquals(BLACK, ColorMath.sourceOver(BLACK, RED))
    }

    @Test
    fun isClose_WithinTolerance_ReturnsTrue() {
        assertTrue(ColorMath.isClose(0xFFFFFFFF.toInt(), 0xFFF0F0F0.toInt(), TOLERANCE))
    }

    private fun canvas(color: (Int, Int) -> Int) = IntArray(SIZE * SIZE) { color(it % SIZE, it / SIZE) }

    private companion object {
        const val SIZE = 7
        const val WALL_X = 3
        const val TOLERANCE = 48
        const val WHITE = 0xFFFFFFFF.toInt()
        const val BLACK = 0xFF000000.toInt()
        const val RED = 0xFFFF0000.toInt()
        const val TRANSPARENT = 0
        const val HALF_BLACK = 0x80000000.toInt()
    }
}
