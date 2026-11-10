package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import dev.sezertanci.drawingworkshop.domain.stroke.FloatList
import dev.sezertanci.drawingworkshop.domain.stroke.RainbowHues
import dev.sezertanci.drawingworkshop.domain.stroke.SprayScatter
import dev.sezertanci.drawingworkshop.domain.stroke.StrokePoint
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Parmağın geçtiği yere rastgele küçük noktalar serpen sprey çizgisi. [hues] verilmişse noktaların rengi
 * parmağın aldığı yola göre gökkuşağı renkleri arasında değişir; aynı renkteki ardışık noktalar bir arada çizilir.
 */
class SprayStroke(
    private val paint: Paint,
    private val radius: Float,
    private val dotsPerTouch: Int,
    private val scatter: SprayScatter,
    private val hues: RainbowHues? = null,
) : ActiveStroke {
    private val dots = FloatList()
    private val runs = ArrayList<ColorRun>()
    private var distance = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var minX = Float.MAX_VALUE
    private var minY = Float.MAX_VALUE
    private var maxX = -Float.MAX_VALUE
    private var maxY = -Float.MAX_VALUE

    override fun begin(x: Float, y: Float) {
        lastX = x
        lastY = y
        spray(x, y)
    }

    override fun extend(x: Float, y: Float) = spray(x, y)

    override fun end() = Unit

    override fun drawPreview(canvas: Canvas) = drawDots(canvas)

    override fun commit(canvas: Canvas) = drawDots(canvas)

    override fun dirtyRegion(): Rect {
        val padding = radius + paint.strokeWidth
        return Rect(
            (minX - padding).toInt(),
            (minY - padding).toInt(),
            ceil(maxX + padding).toInt(),
            ceil(maxY + padding).toInt(),
        )
    }

    private fun spray(x: Float, y: Float) {
        distance += hypot(x - lastX, y - lastY)
        lastX = x
        lastY = y
        val color = hues?.colorAt(distance) ?: paint.color
        if (runs.lastOrNull()?.color != color) runs.add(ColorRun(dots.size, color))
        scatter.scatter(StrokePoint(x, y), radius, dotsPerTouch, dots)
        minX = min(minX, x)
        minY = min(minY, y)
        maxX = max(maxX, x)
        maxY = max(maxY, y)
    }

    private fun drawDots(canvas: Canvas) {
        for ((i, run) in runs.withIndex()) {
            val end = runs.getOrNull(i + 1)?.start ?: dots.size
            paint.color = run.color
            canvas.drawPoints(dots.values, run.start, end - run.start, paint)
        }
    }

    /** [start] konumundaki noktadan bir sonraki gruba kadar aynı renkle çizilen noktalar. */
    private class ColorRun(val start: Int, val color: Int)
}
