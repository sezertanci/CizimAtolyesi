package dev.sezertanci.drawingworkshop.domain.symmetry

import dev.sezertanci.drawingworkshop.domain.geometry.PixelRect
import org.junit.Assert.assertEquals
import org.junit.Test

class SymmetryModeTest {

    @Test
    fun transforms_EachMode_HasExpectedCopyCount() {
        val counts = SymmetryMode.entries.map { it.transforms.size }

        assertEquals(listOf(1, 2, 2, 4, 8), counts)
    }

    @Test
    fun transforms_FirstTransform_IsAlwaysTheStrokeItself() {
        SymmetryMode.entries.forEach { assertEquals(SymmetryTransform.IDENTITY, it.transforms.first()) }
    }

    @Test
    fun transforms_FourParts_MirrorsAcrossBothAxes() {
        val points = SymmetryMode.FOUR.transforms.map { map(it, x = 70f, y = 60f) }.toSet()

        assertEquals(setOf(70f to 60f, 30f to 60f, 70f to 40f, 30f to 40f), points)
    }

    @Test
    fun transforms_EightParts_IsKaleidoscopeAroundCenter() {
        val points = SymmetryMode.EIGHT.transforms.map { map(it, x = 70f, y = 60f) }.toSet()

        val expected = setOf(
            70f to 60f, 30f to 60f, 70f to 40f, 30f to 40f,
            60f to 70f, 40f to 70f, 60f to 30f, 40f to 30f,
        )
        assertEquals(expected, points)
    }

    @Test
    fun mapX_MirrorX_MirrorsAcrossVerticalAxis() {
        assertEquals(30f to 60f, map(SymmetryTransform.MIRROR_X, x = 70f, y = 60f))
    }

    @Test
    fun mapY_MirrorY_MirrorsAcrossHorizontalAxis() {
        assertEquals(70f to 40f, map(SymmetryTransform.MIRROR_Y, x = 70f, y = 60f))
    }

    @Test
    fun mapRect_MirrorX_KeepsExclusiveRightEdge() {
        val mapped = SymmetryTransform.MIRROR_X.mapRect(PixelRect(60, 10, 70, 20), CENTER, CENTER)

        assertEquals(PixelRect(30, 10, 40, 20), mapped)
    }

    @Test
    fun mapRect_Rotate90_SwapsWidthAndHeight() {
        val mapped = SymmetryTransform.ROTATE_90.mapRect(PixelRect(60, 50, 80, 55), CENTER, CENTER)

        assertEquals(PixelRect(45, 60, 50, 80), mapped)
    }

    @Test
    fun mapRect_FractionalCenter_RoundsOutward() {
        val mapped = SymmetryTransform.MIRROR_X.mapRect(PixelRect(10, 0, 20, 5), 50.5f, CENTER)

        assertEquals(PixelRect(81, 0, 91, 5), mapped)
    }

    private fun map(transform: SymmetryTransform, x: Float, y: Float): Pair<Float, Float> =
        transform.mapX(x, y, CENTER, CENTER) to transform.mapY(x, y, CENTER, CENTER)

    private companion object {
        const val CENTER = 50f
    }
}
