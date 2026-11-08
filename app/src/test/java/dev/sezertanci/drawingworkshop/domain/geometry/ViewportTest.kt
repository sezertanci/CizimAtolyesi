package dev.sezertanci.drawingworkshop.domain.geometry

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ViewportTest {
    private lateinit var viewport: Viewport

    @Before
    fun setUp() {
        viewport = Viewport(MAX_SCALE).apply { setSize(WIDTH, HEIGHT) }
    }

    @Test
    fun transform_ZoomAtFocus_KeepsFocusPointUnderFingers() {
        viewport.transform(focusX = 300f, focusY = 200f, scaleFactor = 2f, panX = 0f, panY = 0f)

        assertEquals(2f, viewport.scale, DELTA)
        assertEquals(300f, viewport.toCanvasX(300f), DELTA)
        assertEquals(200f, viewport.toCanvasY(200f), DELTA)
    }

    @Test
    fun transform_BeyondMaxScale_StopsAtMax() {
        viewport.transform(500f, 400f, scaleFactor = 10f, panX = 0f, panY = 0f)

        assertEquals(MAX_SCALE, viewport.scale, DELTA)
    }

    @Test
    fun transform_BelowNormalSize_StaysAtNormalSizeAndCentered() {
        viewport.transform(500f, 400f, scaleFactor = 2f, panX = 0f, panY = 0f)

        viewport.transform(100f, 100f, scaleFactor = 0.1f, panX = 0f, panY = 0f)

        assertEquals(1f, viewport.scale, DELTA)
        assertEquals(0f, viewport.offsetX, DELTA)
        assertEquals(0f, viewport.offsetY, DELTA)
    }

    @Test
    fun transform_PanPastEdge_KeepsCanvasCovered() {
        viewport.transform(0f, 0f, scaleFactor = 2f, panX = 0f, panY = 0f)

        viewport.transform(0f, 0f, scaleFactor = 1f, panX = 5000f, panY = -5000f)

        assertEquals(0f, viewport.offsetX, DELTA)
        assertEquals(-HEIGHT.toFloat(), viewport.offsetY, DELTA)
    }

    @Test
    fun transform_PanWhileZoomed_MovesImage() {
        viewport.transform(0f, 0f, scaleFactor = 2f, panX = 0f, panY = 0f)

        viewport.transform(0f, 0f, scaleFactor = 1f, panX = -100f, panY = -50f)

        assertEquals(50f, viewport.toCanvasX(0f), DELTA)
        assertEquals(25f, viewport.toCanvasY(0f), DELTA)
    }

    @Test
    fun setSize_AfterZoom_ResetsToNormalSize() {
        viewport.transform(500f, 400f, scaleFactor = 3f, panX = 0f, panY = 0f)

        viewport.setSize(WIDTH, HEIGHT)

        assertEquals(1f, viewport.scale, DELTA)
        assertEquals(500f, viewport.toCanvasX(500f), DELTA)
    }

    private companion object {
        const val WIDTH = 1000
        const val HEIGHT = 800
        const val MAX_SCALE = 4f
        const val DELTA = 0.001f
    }
}
