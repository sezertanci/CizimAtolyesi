package dev.sezertanci.drawingworkshop.data.coloring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltInColoringPageCatalogTest {
    private val catalog = BuiltInColoringPageCatalog()

    @Test
    fun pages_All_HaveUniqueIdsAndArt() {
        val pages = catalog.pages

        assertTrue(pages.isNotEmpty())
        assertEquals(pages.size, pages.map(ColoringPage::id).toSet().size)
        assertEquals(pages.size, pages.map(ColoringPage::art).toSet().size)
    }

    @Test
    fun find_KnownId_ReturnsPage() {
        val page = catalog.pages.first()

        assertEquals(page, catalog.find(page.id))
    }

    @Test
    fun find_UnknownId_ReturnsNull() {
        assertNull(catalog.find("missing"))
    }
}
