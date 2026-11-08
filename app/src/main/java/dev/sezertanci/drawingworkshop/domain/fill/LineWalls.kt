package dev.sezertanci.drawingworkshop.domain.fill

/**
 * Boyama sayfasının çizgilerinden boya kovası için duvar haritası çıkarır.
 *
 * Yalnızca çizginin koyu gövdesi duvar sayılır. Kenardaki yumuşak (yarı saydam) pikseller duvar
 * değildir; dolgu onların altına da girer ve çizgi ile renk arasında beyaz boşluk kalmaz.
 */
object LineWalls {
    /** Bu opaklıktan (0–255) koyu çizgi pikselleri duvardır. */
    const val WALL_ALPHA = 192

    fun fromPixels(linePixels: IntArray): BooleanArray =
        BooleanArray(linePixels.size) { ColorMath.alpha(linePixels[it]) >= WALL_ALPHA }
}
