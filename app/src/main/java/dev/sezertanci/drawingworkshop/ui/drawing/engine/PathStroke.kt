package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import dev.sezertanci.drawingworkshop.domain.stroke.QuadSegment
import dev.sezertanci.drawingworkshop.domain.stroke.StrokePoint
import dev.sezertanci.drawingworkshop.domain.stroke.StrokeSmoother

/**
 * Yumuşatılmış bir yol (path) olarak çizilen çizgi. Kalem, fırça, keçeli kalem, pastel boya ve silgi
 * bu sınıfı kullanır; araçlar yalnızca kullandıkları [Paint] ayarlarıyla ayrılır.
 *
 * Önizleme ve kalıcı çizim farklı boyalarla yapılabilir: silgi önizlemede arka plan rengiyle çizer,
 * boya katmanına işlerken ise pikselleri temizler.
 */
class PathStroke(
    private val previewPaint: Paint,
    private val commitPaint: Paint,
    minPointDistance: Float,
) : ActiveStroke {
    private val path = Path()
    private val smoother = StrokeSmoother(minPointDistance)
    private var hasSegments = false
    private var startPoint = StrokePoint(0f, 0f)

    override fun begin(x: Float, y: Float) {
        startPoint = StrokePoint(x, y)
        path.moveTo(x, y)
        smoother.start(startPoint)
    }

    override fun extend(x: Float, y: Float) {
        smoother.add(StrokePoint(x, y))?.let(::append)
    }

    override fun end() {
        smoother.finish()?.let(::append)
        if (!hasSegments) path.lineTo(startPoint.x + DOT_OFFSET, startPoint.y)
    }

    override fun drawPreview(canvas: Canvas) {
        canvas.drawPath(path, previewPaint)
    }

    override fun commit(canvas: Canvas) {
        canvas.drawPath(path, commitPaint)
    }

    override fun dirtyRegion(): Rect {
        val bounds = RectF()
        path.computeBounds(bounds, true)
        return bounds.toDirtyRegion(commitPaint.strokeWidth)
    }

    private fun append(segment: QuadSegment) {
        path.quadTo(segment.control.x, segment.control.y, segment.end.x, segment.end.y)
        hasSegments = true
    }

    private companion object {
        /** Dokunup hemen kaldırınca da bir nokta görünsün diye yola eklenen çok kısa çizginin uzunluğu. */
        const val DOT_OFFSET = 0.1f
    }
}
