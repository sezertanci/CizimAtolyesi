package dev.sezertanci.drawingworkshop.domain.symmetry

import dev.sezertanci.drawingworkshop.domain.geometry.PixelRect
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Simetri merkezi çevresinde ayna veya dörtte bir tur döndürme.
 *
 * Merkeze göre konum (dx, dy) şu noktaya gider: (xx·dx + xy·dy, yx·dx + yy·dy). Katsayılar yalnızca
 * -1, 0 ve 1 olur; bu yüzden eksenlere paralel bir dikdörtgen yine eksenlere paralel bir dikdörtgene gider.
 */
data class SymmetryTransform(val xx: Int, val xy: Int, val yx: Int, val yy: Int) {

    fun mapX(x: Float, y: Float, centerX: Float, centerY: Float): Float =
        centerX + xx * (x - centerX) + xy * (y - centerY)

    fun mapY(x: Float, y: Float, centerX: Float, centerY: Float): Float =
        centerY + yx * (x - centerX) + yy * (y - centerY)

    /** Dikdörtgenin karşılığı; kesirli kenarlar dışa doğru yuvarlanır, böylece hiçbir piksel dışarıda kalmaz. */
    fun mapRect(rect: PixelRect, centerX: Float, centerY: Float): PixelRect {
        val left = rect.left.toFloat()
        val top = rect.top.toFloat()
        val right = rect.right.toFloat()
        val bottom = rect.bottom.toFloat()
        val x1 = mapX(left, top, centerX, centerY)
        val y1 = mapY(left, top, centerX, centerY)
        val x2 = mapX(right, bottom, centerX, centerY)
        val y2 = mapY(right, bottom, centerX, centerY)
        return PixelRect(
            floor(minOf(x1, x2)).toInt(),
            floor(minOf(y1, y2)).toInt(),
            ceil(maxOf(x1, x2)).toInt(),
            ceil(maxOf(y1, y2)).toInt(),
        )
    }

    companion object {
        val IDENTITY = SymmetryTransform(1, 0, 0, 1)

        /** Dikey eksene göre ayna: sol ile sağ yer değiştirir. */
        val MIRROR_X = SymmetryTransform(-1, 0, 0, 1)

        /** Yatay eksene göre ayna: üst ile alt yer değiştirir. */
        val MIRROR_Y = SymmetryTransform(1, 0, 0, -1)
        val ROTATE_90 = SymmetryTransform(0, -1, 1, 0)
        val ROTATE_180 = SymmetryTransform(-1, 0, 0, -1)
        val ROTATE_270 = SymmetryTransform(0, 1, -1, 0)

        /** Sol üstten sağ alta inen köşegene göre ayna. */
        val MIRROR_DIAGONAL = SymmetryTransform(0, 1, 1, 0)

        /** Sol alttan sağ üste çıkan köşegene göre ayna. */
        val MIRROR_ANTI_DIAGONAL = SymmetryTransform(0, -1, -1, 0)
    }
}
