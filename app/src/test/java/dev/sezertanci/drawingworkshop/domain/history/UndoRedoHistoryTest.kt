package dev.sezertanci.drawingworkshop.domain.history

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UndoRedoHistoryTest {
    private val discarded = mutableListOf<String>()
    private val history = UndoRedoHistory<String>(CAPACITY) { discarded += it }

    @Test
    fun record_MoreThanCapacity_DiscardsOldest() {
        repeat(CAPACITY + 1) { history.record("e$it") }

        assertEquals(listOf("e0"), discarded)
        var undoCount = 0
        while (history.undo { "r$it" }) undoCount++
        assertEquals(CAPACITY, undoCount)
    }

    @Test
    fun undo_AfterRecord_StoresInverseForRedo() {
        history.record("draw")

        history.undo { "inverse-of-$it" }
        var redone: String? = null
        history.redo { redone = it; it }

        assertEquals("inverse-of-draw", redone)
    }

    @Test
    fun record_AfterUndo_ClearsRedoAndDiscardsIt() {
        history.record("a")
        history.undo { "a-inverse" }

        history.record("b")

        assertFalse(history.canRedo)
        assertEquals(listOf("a-inverse"), discarded)
    }

    @Test
    fun undo_EmptyHistory_ReturnsFalse() {
        val result = history.undo { it }

        assertFalse(result)
        assertFalse(history.canUndo)
    }

    @Test
    fun clear_WithEntries_DiscardsAll() {
        history.record("a")
        history.record("b")
        history.undo { "b-inverse" }

        history.clear()

        assertTrue(discarded.containsAll(listOf("a", "b-inverse")))
        assertFalse(history.canUndo)
        assertFalse(history.canRedo)
    }

    private companion object {
        const val CAPACITY = 5
    }
}
