package dev.sezertanci.drawingworkshop.ui.gallery

import dev.sezertanci.drawingworkshop.data.DrawingPage

/**
 * Galerinin kaydırdıkça büyüyen yükleme sınırı. Sınır bir sayfa artar; sonraki sayfa yalnızca önceki
 * sayfa gelip sırada başka resim varsa istenir, böylece hızlı kaydırmada aynı sayfa iki kez istenmez.
 */
object GalleryPaging {
    /** Bir seferde yüklenen resim sayısı; tabletin bir ekranından biraz fazlası. */
    const val PAGE_SIZE = 30

    /** Listenin sonuna bu kadar kart kala sonraki sayfa istenir. */
    const val PREFETCH_DISTANCE = 10

    fun nextLimit(currentLimit: Int, loaded: DrawingPage): Int {
        val pageArrived = loaded.drawings.size >= currentLimit
        return if (pageArrived && loaded.hasMore) currentLimit + PAGE_SIZE else currentLimit
    }

    /** Görünen son kart listenin sonuna [PREFETCH_DISTANCE] kadar yaklaştı mı? */
    fun isNearEnd(lastVisibleIndex: Int, totalCount: Int): Boolean =
        totalCount > 0 && lastVisibleIndex >= totalCount - PREFETCH_DISTANCE
}
