package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import dev.sezertanci.drawingworkshop.domain.symmetry.SymmetryMode
import javax.inject.Inject

/**
 * Simetri açıkken ayna eksenlerini ince kesikli çizgilerle gösterir. Yalnızca ekrana çizilir; kayda,
 * küçük resme ve dışa aktarmaya girmez. Kesikli çizginin altındaki yarı saydam beyaz şerit, çizgiyi
 * koyu arka planlarda da görünür kılar. Kalınlık büyütme oranına bölünür, ekranda hep aynı görünür.
 */
class SymmetryGuides @Inject constructor(private val density: ScreenDensity) {
    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = HALO_COLOR
    }
    private val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = DASH_COLOR
    }
    private var zoom = 0f

    fun draw(canvas: Canvas, mode: SymmetryMode, width: Int, height: Int, zoom: Float) {
        if (mode.axes.isEmpty()) return
        applyZoom(zoom)
        val centerX = width / 2f
        val centerY = height / 2f
        val length = (width + height).toFloat()
        val saved = canvas.save()
        canvas.clipRect(0, 0, width, height)
        for (axis in mode.axes) {
            val dx = axis.dx * length
            val dy = axis.dy * length
            canvas.drawLine(centerX - dx, centerY - dy, centerX + dx, centerY + dy, haloPaint)
            canvas.drawLine(centerX - dx, centerY - dy, centerX + dx, centerY + dy, dashPaint)
        }
        canvas.restoreToCount(saved)
    }

    private fun applyZoom(zoom: Float) {
        if (this.zoom == zoom) return
        this.zoom = zoom
        val width = density.toPx(LINE_WIDTH_DP) / zoom
        val dash = density.toPx(DASH_LENGTH_DP) / zoom
        dashPaint.strokeWidth = width
        dashPaint.pathEffect = DashPathEffect(floatArrayOf(dash, dash), 0f)
        haloPaint.strokeWidth = width * HALO_WIDTH_FACTOR
    }

    private companion object {
        const val LINE_WIDTH_DP = 1.5f
        const val DASH_LENGTH_DP = 8f
        const val HALO_WIDTH_FACTOR = 2.5f
        const val DASH_COLOR = 0xA0606060.toInt()
        const val HALO_COLOR = 0x66FFFFFF
    }
}
