package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import dev.sezertanci.drawingworkshop.domain.stroke.QuadSegment
import dev.sezertanci.drawingworkshop.domain.stroke.RainbowCycle
import dev.sezertanci.drawingworkshop.domain.stroke.StrokePoint
import dev.sezertanci.drawingworkshop.domain.stroke.StrokeSmoother
import kotlin.math.hypot

/**
 * Rengi çizgi boyunca kendiliğinden değişen gökkuşağı çizgisi.
 *
 * Yol, aynı renk kademesine düşen ardışık parçalardan oluşan bölümlere ayrılır; her bölüm kendi
 * rengiyle tek bir yol olarak çizilir. Böylece uzun bir çizgide bile çizim çağrısı sayısı az kalır.
 *
 * Bölümlerin uçları düz kesilir, böylece renkler arasında boncuk gibi yuvarlak izler oluşmaz. Kıvrımlarda
 * aralık kalmasın diye her bölüm, bir önceki bölümün son parçasını yeniden çizerek başlar. Çizginin
 * yalnızca baş ve son ucu yuvarlatılır.
 */
class RainbowStroke(
    private val paint: Paint,
    minPointDistance: Float,
    private val cycle: RainbowCycle,
) : ActiveStroke {
    private val sections = ArrayList<Section>()
    private val smoother = StrokeSmoother(minPointDistance)
    private val capPaint = Paint(paint).apply { style = Paint.Style.FILL }
    private var startPoint = StrokePoint(0f, 0f)
    private var lastEnd = startPoint
    private var lastSegmentStart = startPoint
    private var lastSegment: QuadSegment? = null
    private var distance = 0f

    init {
        paint.strokeCap = Paint.Cap.BUTT
    }

    override fun begin(x: Float, y: Float) {
        startPoint = StrokePoint(x, y)
        lastEnd = startPoint
        startSection(cycle.stepAt(distance)).moveTo(x, y)
        smoother.start(startPoint)
    }

    override fun extend(x: Float, y: Float) {
        smoother.add(StrokePoint(x, y))?.let(::append)
    }

    override fun end() {
        smoother.finish()?.let(::append)
    }

    override fun drawPreview(canvas: Canvas) = drawSections(canvas)

    override fun commit(canvas: Canvas) = drawSections(canvas)

    override fun dirtyRegion(): Rect {
        val total = RectF(startPoint.x, startPoint.y, startPoint.x, startPoint.y)
        val bounds = RectF()
        for (section in sections) {
            section.path.computeBounds(bounds, true)
            total.union(bounds)
        }
        return total.toDirtyRegion(paint.strokeWidth)
    }

    private fun append(segment: QuadSegment) {
        distance += hypot(segment.end.x - lastEnd.x, segment.end.y - lastEnd.y)
        val step = cycle.stepAt(distance)
        val path = if (step == sections.last().step) sections.last().path else startOverlappingSection(step)
        path.quadTo(segment.control.x, segment.control.y, segment.end.x, segment.end.y)
        lastSegmentStart = lastEnd
        lastSegment = segment
        lastEnd = segment.end
    }

    /** Yeni bölümü bir önceki bölümün son parçasıyla başlatır; bu örtüşme kıvrımlardaki aralığı kapatır. */
    private fun startOverlappingSection(step: Int): Path {
        val path = startSection(step)
        val previous = lastSegment
        if (previous == null) {
            path.moveTo(lastEnd.x, lastEnd.y)
        } else {
            path.moveTo(lastSegmentStart.x, lastSegmentStart.y)
            path.quadTo(previous.control.x, previous.control.y, previous.end.x, previous.end.y)
        }
        return path
    }

    private fun startSection(step: Int): Path {
        val section = Section(step, RainbowCycle.colorOf(step))
        sections.add(section)
        return section.path
    }

    private fun drawSections(canvas: Canvas) {
        for (section in sections) {
            paint.color = section.color
            canvas.drawPath(section.path, paint)
        }
        drawCap(canvas, startPoint, sections.first().color)
        drawCap(canvas, lastEnd, sections.last().color)
    }

    private fun drawCap(canvas: Canvas, point: StrokePoint, color: Int) {
        capPaint.color = color
        canvas.drawCircle(point.x, point.y, paint.strokeWidth / 2f, capPaint)
    }

    private class Section(val step: Int, val color: Int) {
        val path = Path()
    }
}
