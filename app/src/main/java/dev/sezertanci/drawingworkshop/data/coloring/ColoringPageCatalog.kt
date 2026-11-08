package dev.sezertanci.drawingworkshop.data.coloring

/** Uygulamadaki boyama sayfalarının listesi. */
interface ColoringPageCatalog {
    val pages: List<ColoringPage>

    /** Kimlik tanınmıyorsa (ör. kaldırılmış bir sayfa) `null` döner. */
    fun find(id: String): ColoringPage?
}
