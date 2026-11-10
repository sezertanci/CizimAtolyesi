package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import dev.sezertanci.drawingworkshop.domain.stroke.QuadSegment
import dev.sezertanci.drawingworkshop.domain.stroke.RainbowHues
import dev.sezertanci.drawingworkshop.domain.stroke.StrokePoint
import dev.sezertanci.drawingworkshop.domain.stroke.StrokeSmoother
import dev.sezertanci.drawingworkshop.domain.stroke.divide
import kotlin.math.hypot

/**
 * Rengi çizgi boyunca kendiliğinden ve rastgele değişen gökkuşağı çizgisi.
 *
 * Hızlı çizimde dokunma noktaları seyrek gelir. Bu yüzden her eğri parçası önce kısa düz parçalara
 * bölünür ve renk her parçanın sonunda ayrıca hesaplanır; uzun bir parçada ara tonlar atlanmaz.
 *
 * Parçalar, [sectionLength] uzunluğundaki bölümlerde toplanır. Her bölüm başındaki renkten sonundaki
 * renge doğrusal geçişle boyanır. Bölümlerin uçları düz kesilir, böylece renkler arasında boncuk gibi
 * yuvarlak izler oluşmaz. Kıvrımlarda aralık kalmasın diye her bölüm, bir önceki bölümün son parçasını
 * aynı renklerle yeniden çizerek başlar. Çizginin yalnızca baş ve son ucu yuvarlatılır.
 */
class RainbowStroke(
    private val paint: Paint,
    minPointDistance: Float,
    private val sectionLength: Float,
    private val hues: RainbowHues,
) : ActiveStroke {
    private val sections = ArrayList<Section>()
    private val smoother = StrokeSmoother(minPointDistance)
    private val capPaint = Paint(paint).apply { style = Paint.Style.FILL }
    private val maxPieceLength = sectionLength / PIECES_PER_SECTION
    private var startPoint = StrokePoint(0f, 0f)
    private var lastEnd = startPoint
    private var lastPieceStart = startPoint
    private var lastPieceStartDistance = 0f
    private var distance = 0f
    private var sectionStartDistance = 0f

    init {
        paint.strokeCap = Paint.Cap.BUTT
    }

    override fun begin(x: Float, y: Float) {
        startPoint = StrokePoint(x, y)
        lastEnd = startPoint
        lastPieceStart = startPoint
        sections.add(Section(startPoint, hues.colorAt(0f)))
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
        segment.divide(lastEnd, maxPieceLength).forEach(::appendPiece)
    }

    private fun appendPiece(point: StrokePoint) {
        if (distance - sectionStartDistance >= sectionLength) startOverlappingSection()
        lastPieceStart = lastEnd
        lastPieceStartDistance = distance
        distance += hypot(point.x - lastEnd.x, point.y - lastEnd.y)
        sections.last().extendTo(point, hues.colorAt(distance))
        lastEnd = point
    }

    /** Yeni bölüm, bir önceki bölümün son parçasını aynı renklerle yeniden çizerek başlar. */
    private fun startOverlappingSection() {
        sectionStartDistance = distance
        val overlap = Section(lastPieceStart, hues.colorAt(lastPieceStartDistance))
        overlap.extendTo(lastEnd, hues.colorAt(distance))
        sections.add(overlap.withJoint())
    }

    private fun drawSections(canvas: Canvas) {
        for (section in sections) {
            section.applyTo(paint)
            canvas.drawPath(section.path, paint)
        }
        paint.shader = null
        drawCap(canvas, startPoint, sections.first().startColor)
        drawCap(canvas, lastEnd, sections.last().endColor)
    }

    private fun drawCap(canvas: Canvas, point: StrokePoint, color: Int) {
        capPaint.color = color
        canvas.drawCircle(point.x, point.y, paint.strokeWidth / 2f, capPaint)
    }

    /**
     * Başlangıç noktasından bitiş noktasına doğru boyanan bölüm. Örtüşen bir parçayla başlıyorsa
     * renk geçişi, parçanın bittiği ara noktada da önceki bölümün rengini tutturur.
     */
    private class Section(private val start: StrokePoint, val startColor: Int) {
        val path = Path().apply { moveTo(start.x, start.y) }
        var endColor = startColor
            private set
        private var end = start
        private var joint: StrokePoint? = null
        private var jointColor = startColor
        private var gradient: Shader? = null

        fun extendTo(point: StrokePoint, color: Int) {
            path.lineTo(point.x, point.y)
            end = point
            endColor = color
            gradient = null
        }

        /** Şu ana kadarki bitiş noktasını, renk geçişinin ara durağı olarak işaretler. */
        fun withJoint(): Section = apply {
            joint = end
            jointColor = endColor
        }

        /** Bölüm henüz bir noktadan ibaretse geçiş çizilemez; o zaman düz renk kullanılır. */
        fun applyTo(paint: Paint) {
            val length = hypot(end.x - start.x, end.y - start.y)
            if (length < MIN_GRADIENT_LENGTH) {
                paint.shader = null
                paint.color = startColor
                return
            }
            paint.shader = gradient ?: createGradient(length).also { gradient = it }
        }

        private fun createGradient(length: Float): Shader {
            val stop = joint?.let { jointPosition(it, length) } ?: 0f
            return LinearGradient(
                start.x, start.y, end.x, end.y,
                intArrayOf(startColor, jointColor, endColor),
                floatArrayOf(0f, stop, 1f),
                Shader.TileMode.CLAMP,
            )
        }

        /** [point] noktasının, başlangıçtan bitişe uzanan doğru üzerindeki oransal yeri (0 ile 1 arası). */
        private fun jointPosition(point: StrokePoint, length: Float): Float {
            val dot = (point.x - start.x) * (end.x - start.x) + (point.y - start.y) * (end.y - start.y)
            return (dot / (length * length)).coerceIn(0f, 1f)
        }
    }

    private companion object {
        const val MIN_GRADIENT_LENGTH = 0.5f

        /** Bir bölüm en az bu kadar düz parçaya bölünür; eğriler köşeli görünmez. */
        const val PIECES_PER_SECTION = 3f
    }
}
