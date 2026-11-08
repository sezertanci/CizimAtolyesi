package dev.sezertanci.drawingworkshop.domain.sticker

/** Çıkartma panelinde sunulan emojiler: hayvanlar, doğa, yiyecekler ve oyuncaklar. */
object StickerCatalog {
    val stickers: List<String> = listOf(
        "🐶", "🐱", "🐰", "🦊", "🐻", "🐼", "🦁", "🐸",
        "🐵", "🦄", "🐞", "🦋", "🐝", "🐠", "🐢", "🌸",
        "🌻", "🌳", "☀️", "🌙", "⭐", "☁️", "❤️", "🎈",
        "🍎", "🍓", "🍦", "🎁", "🚗", "🚀", "⚽", "🏠",
    )

    val default: String = stickers.first()
}
