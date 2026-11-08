package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import dev.sezertanci.drawingworkshop.domain.stroke.FloatList
import dev.sezertanci.drawingworkshop.domain.stroke.SprayScatter
import dev.sezertanci.drawingworkshop.domain.stroke.StrokePoint
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/** Parmağın geçtiği yere rastgele küçük noktalar serpen sprey çizgisi. */
class SprayStroke(
    private val paint: Paint,
    private val radius: Float,
    private val dotsPerTouch: Int,
    private val scatter: SprayScatter,
) : ActiveStroke {
    private val dots = FloatList()
    private var minX = Float.MAX_VALUE
    private var minY = Float.MAX_VALUE
    private var maxX = -Float.MAX_VALUE
    private var maxY = -Float.MAX_VALUE

    override fun begin(x: Float, y: Float) = spray(x, y)

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
        scatter.scatter(StrokePoint(x, y), radius, dotsPerTouch, dots)
        minX = min(minX, x)
        minY = min(minY, y)
        maxX = max(maxX, x)
        maxY = max(maxY, y)
    }

    private fun drawDots(canvas: Canvas) {
        canvas.drawPoints(dots.values, 0, dots.size, paint)
    }
}
