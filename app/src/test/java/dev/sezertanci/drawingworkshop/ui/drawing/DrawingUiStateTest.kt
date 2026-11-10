package dev.sezertanci.drawingworkshop.ui.drawing

import dev.sezertanci.drawingworkshop.domain.model.ToolType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawingUiStateTest {
    @Test
    fun drawsRainbow_SupportingTools_ReturnTrue() {
        for (tool in listOf(ToolType.PENCIL, ToolType.BRUSH, ToolType.MARKER, ToolType.SPRAY)) {
            assertTrue(tool.name, DrawingUiState(tool = tool, rainbow = true).drawsRainbow)
        }
    }

    @Test
    fun drawsRainbow_UnsupportedTools_ReturnFalse() {
        for (tool in listOf(ToolType.BUCKET, ToolType.CRAYON, ToolType.SILK, ToolType.ERASER)) {
            assertFalse(tool.name, DrawingUiState(tool = tool, rainbow = true).drawsRainbow)
        }
    }

    @Test
    fun isRainbowAvailable_BackgroundTarget_ReturnsFalse() {
        val state = DrawingUiState(tool = ToolType.BRUSH, colorTarget = ColorTarget.BACKGROUND)

        assertFalse(state.isRainbowAvailable)
    }

    @Test
    fun isRainbowSelected_BackgroundTargetWithRainbow_KeepsDrawingRainbow() {
        val state = DrawingUiState(tool = ToolType.BRUSH, rainbow = true, colorTarget = ColorTarget.BACKGROUND)

        assertFalse(state.isRainbowSelected)
        assertTrue(state.drawsRainbow)
    }

    @Test
    fun isRainbowSelected_BucketAfterRainbow_ShowsLastSolidColor() {
        val state = DrawingUiState(tool = ToolType.BUCKET, rainbow = true, strokeColor = RED)

        assertFalse(state.isRainbowSelected)
        assertEquals(RED, state.selectedColor)
    }

    private companion object {
        const val RED = 0xFFFF0000.toInt()
    }
}
