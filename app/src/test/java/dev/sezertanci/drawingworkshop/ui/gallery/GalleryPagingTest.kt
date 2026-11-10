package dev.sezertanci.drawingworkshop.ui.gallery

import dev.sezertanci.drawingworkshop.data.DrawingPage
import dev.sezertanci.drawingworkshop.domain.drawing.Drawing
import dev.sezertanci.drawingworkshop.ui.gallery.GalleryPaging.PAGE_SIZE
import dev.sezertanci.drawingworkshop.ui.gallery.GalleryPaging.PREFETCH_DISTANCE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GalleryPagingTest {

    @Test
    fun nextLimit_PageArrivedWithMore_GrowsByOnePage() {
        assertEquals(2 * PAGE_SIZE, GalleryPaging.nextLimit(PAGE_SIZE, page(PAGE_SIZE, hasMore = true)))
    }

    @Test
    fun nextLimit_NoMoreDrawings_KeepsLimit() {
        assertEquals(PAGE_SIZE, GalleryPaging.nextLimit(PAGE_SIZE, page(PAGE_SIZE, hasMore = false)))
    }

    @Test
    fun nextLimit_PreviousPageNotArrivedYet_KeepsLimit() {
        val limit = 2 * PAGE_SIZE

        assertEquals(limit, GalleryPaging.nextLimit(limit, page(PAGE_SIZE, hasMore = true)))
    }

    @Test
    fun isNearEnd_LastVisibleWithinPrefetchDistance_ReturnsTrue() {
        val total = 3 * PREFETCH_DISTANCE

        assertTrue(GalleryPaging.isNearEnd(total - PREFETCH_DISTANCE, total))
    }

    @Test
    fun isNearEnd_LastVisibleFarFromEnd_ReturnsFalse() {
        val total = 3 * PREFETCH_DISTANCE

        assertFalse(GalleryPaging.isNearEnd(total - PREFETCH_DISTANCE - 1, total))
    }

    @Test
    fun isNearEnd_EmptyGrid_ReturnsFalse() {
        assertFalse(GalleryPaging.isNearEnd(lastVisibleIndex = -1, totalCount = 0))
    }

    private fun page(size: Int, hasMore: Boolean) =
        DrawingPage(List(size) { Drawing("d$it", updatedAt = 0, deletedAt = null, backgroundColor = 0) }, hasMore)
}
