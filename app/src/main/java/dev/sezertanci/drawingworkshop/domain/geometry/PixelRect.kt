package dev.sezertanci.drawingworkshop.domain.geometry

/** Piksel cinsinden dikdörtgen. [right] ve [bottom] dahil değildir. */
data class PixelRect(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top

    fun contains(x: Int, y: Int): Boolean = x in left until right && y in top until bottom

    /** Kenarları değen dikdörtgenler kesişmiş sayılmaz. */
    fun intersects(other: PixelRect): Boolean =
        left < other.right && other.left < right && top < other.bottom && other.top < bottom

    fun union(other: PixelRect): PixelRect = PixelRect(
        minOf(left, other.left),
        minOf(top, other.top),
        maxOf(right, other.right),
        maxOf(bottom, other.bottom),
    )
}

/** Kesişen dikdörtgenleri birleştirir; sonuçtaki dikdörtgenlerin hiçbiri birbiriyle kesişmez. */
fun List<PixelRect>.mergeOverlapping(): List<PixelRect> {
    val result = toMutableList()
    var index = 0
    while (index < result.size) {
        val other = (index + 1 until result.size).firstOrNull { result[index].intersects(result[it]) }
        if (other == null) {
            index++
        } else {
            // Birleşen dikdörtgen büyüdüğü için öncekilerle yeniden karşılaştırılır.
            result[index] = result[index].union(result.removeAt(other))
            index = 0
        }
    }
    return result
}
