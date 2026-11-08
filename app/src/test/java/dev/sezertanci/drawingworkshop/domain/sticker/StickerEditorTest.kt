package dev.sezertanci.drawingworkshop.domain.sticker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class StickerEditorTest {
    private val editor = StickerEditor(StickerMetrics(defaultSize = 100f, minSize = 40f, maxSize = 400f, touchRadius = 20f))

    @Test
    fun touchStart_NoDraft_PlacesStickerAtTouch() {
        val result = editor.touchStart(EMOJI, 200f, 200f, zoom = 1f)

        assertEquals(StickerTouchResult.EDITING, result)
        assertEquals(StickerDraft(EMOJI, 200f, 200f, 100f), editor.draft)
    }

    @Test
    fun touchStart_Zoomed_PlacesSmallerSticker() {
        editor.touchStart(EMOJI, 200f, 200f, zoom = 2f)

        assertEquals(50f, editor.draft?.size)
    }

    @Test
    fun touchMove_DraggingSticker_MovesByDelta() {
        place()
        editor.touchStart(EMOJI, 210f, 190f, zoom = 1f)

        editor.touchMove(240f, 220f)

        assertEquals(230f, editor.draft?.centerX)
        assertEquals(230f, editor.draft?.centerY)
    }

    @Test
    fun touchMove_ResizeHandle_ResizesAroundCenter() {
        place()
        editor.touchStart(EMOJI, 250f, 250f, zoom = 1f)

        editor.touchMove(300f, 280f)

        assertEquals(StickerDraft(EMOJI, 200f, 200f, 200f), editor.draft)
    }

    @Test
    fun touchMove_ResizeBelowMinimum_ClampsSize() {
        place()
        editor.touchStart(EMOJI, 250f, 250f, zoom = 1f)

        editor.touchMove(201f, 201f)

        assertEquals(40f, editor.draft?.size)
    }

    @Test
    fun touchMove_AfterTouchEnd_DoesNothing() {
        place()
        editor.touchEnd()

        editor.touchMove(300f, 300f)

        assertEquals(200f, editor.draft?.centerX)
    }

    @Test
    fun touchStart_ConfirmButton_ReturnsConfirmedAndKeepsDraft() {
        place()

        val result = editor.touchStart(EMOJI, 250f, 150f, zoom = 1f)

        assertEquals(StickerTouchResult.CONFIRMED, result)
        assertNotNull(editor.draft)
    }

    @Test
    fun touchStart_CancelButton_ReturnsCancelledAndClearsDraft() {
        place()

        val result = editor.touchStart(EMOJI, 150f, 150f, zoom = 1f)

        assertEquals(StickerTouchResult.CANCELLED, result)
        assertNull(editor.draft)
    }

    @Test
    fun touchStart_OutsideSticker_JumpsToTouch() {
        place()

        editor.touchStart(EMOJI, 500f, 400f, zoom = 1f)

        assertEquals(500f, editor.draft?.centerX)
        assertEquals(400f, editor.draft?.centerY)
    }

    @Test
    fun changeEmoji_OpenDraft_KeepsPositionAndSize() {
        place()

        editor.changeEmoji(OTHER_EMOJI)

        assertEquals(StickerDraft(OTHER_EMOJI, 200f, 200f, 100f), editor.draft)
    }

    @Test
    fun take_OpenDraft_ReturnsAndClearsDraft() {
        place()

        val taken = editor.take()

        assertEquals(EMOJI, taken?.emoji)
        assertNull(editor.draft)
    }

    /** Merkezi (200, 200), boyu 100 olan bir çıkartma koyar: düğmeler köşelerdedir. */
    private fun place() {
        editor.touchStart(EMOJI, 200f, 200f, zoom = 1f)
        editor.touchEnd()
    }

    private companion object {
        const val EMOJI = "🐶"
        const val OTHER_EMOJI = "🐱"
    }
}
