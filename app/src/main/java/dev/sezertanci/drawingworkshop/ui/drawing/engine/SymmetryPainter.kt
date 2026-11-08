package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.Rect
import dev.sezertanci.drawingworkshop.domain.geometry.mergeOverlapping
import dev.sezertanci.drawingworkshop.domain.symmetry.SymmetryMode
import dev.sezertanci.drawingworkshop.domain.symmetry.SymmetryTransform

/**
 * Bir simetri modunu tuvalin ortasına göre uygular. Çizgiler değişmeden kalır; her kopya aynı çizginin
 * dönüşüm matrisiyle yeniden çizilmesidir. Böylece sprey noktaları ve gökkuşağı renkleri de birebir
 * aynalanır. Simetri kapalıyken tek kopya, yani çizginin kendisi çizilir.
 */
class SymmetryPainter(private val mode: SymmetryMode, private val width: Int, private val height: Int) {
    private val centerX = width / 2f
    private val centerY = height / 2f
    private val matrices = mode.transforms.map { it.toMatrix() }

    fun drawCopies(canvas: Canvas, block: (Canvas) -> Unit) {
        for (matrix in matrices) {
            val saved = canvas.save()
            canvas.concat(matrix)
            block(canvas)
            canvas.restoreToCount(saved)
        }
    }

    /** Kopyaların etkilediği bölgeler. Kesişenler birleştirilir; böylece geri al kaydı aynı pikseli iki kez tutmaz. */
    fun regions(region: Rect): List<Rect> {
        val source = region.toPixelRect()
        return mode.transforms.map { it.mapRect(source, centerX, centerY) }.mergeOverlapping().map { it.toRect() }
    }

    /** Noktanın tuvalin içine düşen kopyaları; simetrik doldurmada başlangıç noktaları olarak kullanılır. */
    fun points(x: Float, y: Float): List<Point> = mode.transforms
        .map { Point(it.mapX(x, y, centerX, centerY).toInt(), it.mapY(x, y, centerX, centerY).toInt()) }
        .filter { it.x in 0 until width && it.y in 0 until height }

    private fun SymmetryTransform.toMatrix(): Matrix = Matrix().apply {
        setValues(
            floatArrayOf(
                xx.toFloat(), xy.toFloat(), centerX - xx * centerX - xy * centerY,
                yx.toFloat(), yy.toFloat(), centerY - yx * centerX - yy * centerY,
                0f, 0f, 1f,
            ),
        )
    }
}
