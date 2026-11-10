package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import dev.sezertanci.drawingworkshop.domain.stroke.FloatList
import dev.sezertanci.drawingworkshop.domain.stroke.SilkMotion
import kotlin.math.sin

/**
 * İpek fırçası: parmağı gecikerek izleyen ince tellerden oluşan, ışıl ışıl bir şerit.
 *
 * Her hareket adımında iki şey çizilir: tellerin kısa izleri ve tellerin hepsinden geçen çok saydam bir
 * eğri. Bu eğriler üst üste bindikçe teller arasında ipek gibi yarı saydam bir tabaka oluşur. Renk, seçili
 * rengin tonu çevresinde zamanla hafifçe kayar. Boyalar, zemine uygun karışımla gelir (bkz. [StrokeFactory]).
 *
 * Bir karede oluşan adımlar biriktirilir ve [drawPending] ile tek seferde çizilir.
 */
class SilkStroke(
    private val motion: SilkMotion,
    private val threadPaint: Paint,
    private val sheetPaint: Paint,
    baseColor: Int,
) : LiveStroke {
    private val baseHsv = FloatArray(HSV_SIZE).also { Color.colorToHSV(baseColor, it) }
    private val hsv = FloatArray(HSV_SIZE)
    private val threadAlpha = threadPaint.alpha
    private val sheetAlpha = sheetPaint.alpha

    // Her adım için: adımın zamanı, ardından her telin önceki ve şimdiki konumu (x, y, x, y).
    private val pending = FloatList()
    private val bounds = RectF()
    private val path = Path()
    private val floatsPerStep = 1 + motion.threadCount * FLOATS_PER_THREAD

    override fun begin(x: Float, y: Float) = motion.start(x, y)

    override fun moveTo(x: Float, y: Float) = motion.moveTo(x, y)

    override fun advance(seconds: Float): Rect? {
        pending.clear()
        bounds.set(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE)
        motion.advance(seconds, ::recordStep)
        if (pending.size == 0) return null
        return bounds.toDirtyRegion(threadPaint.strokeWidth)
    }

    override fun drawPending(canvas: Canvas) {
        val values = pending.values
        for (start in 0 until pending.size step floatsPerStep) {
            applyColor(values[start])
            canvas.drawPath(threadsPath(values, start + 1), threadPaint)
            canvas.drawPath(sheetPath(values, start + 1), sheetPaint)
        }
    }

    private fun recordStep() {
        pending.add(motion.time)
        for (i in 0 until motion.threadCount) {
            pending.add(motion.previousX(i))
            pending.add(motion.previousY(i))
            pending.add(motion.x(i))
            pending.add(motion.y(i))
            bounds.include(motion.previousX(i), motion.previousY(i))
            bounds.include(motion.x(i), motion.y(i))
        }
    }

    /** Dikdörtgeni noktayı kapsayacak kadar büyütür; ters kurulmuş boş dikdörtgenle de doğru çalışır. */
    private fun RectF.include(x: Float, y: Float) {
        set(minOf(left, x), minOf(top, y), maxOf(right, x), maxOf(bottom, y))
    }

    /** Tellerin bir önceki adımdan bu adıma uzanan izleri. */
    private fun threadsPath(values: FloatArray, first: Int): Path {
        path.rewind()
        for (i in 0 until motion.threadCount) {
            val at = first + i * FLOATS_PER_THREAD
            path.moveTo(values[at], values[at + 1])
            path.lineTo(values[at + 2], values[at + 3])
        }
        return path
    }

    /** Tellerin şimdiki konumlarından sırayla geçen yumuşak eğri; ara noktalar köşe yapmaz. */
    private fun sheetPath(values: FloatArray, first: Int): Path {
        path.rewind()
        val last = motion.threadCount - 1
        path.moveTo(currentX(values, first, 0), currentY(values, first, 0))
        for (i in 1 until last) {
            val x = currentX(values, first, i)
            val y = currentY(values, first, i)
            path.quadTo(x, y, (x + currentX(values, first, i + 1)) / 2f, (y + currentY(values, first, i + 1)) / 2f)
        }
        path.lineTo(currentX(values, first, last), currentY(values, first, last))
        return path
    }

    private fun currentX(values: FloatArray, first: Int, thread: Int) = values[first + thread * FLOATS_PER_THREAD + 2]

    private fun currentY(values: FloatArray, first: Int, thread: Int) = values[first + thread * FLOATS_PER_THREAD + 3]

    /** Seçili rengin tonu, zamanla iki yana salınır. */
    private fun applyColor(time: Float) {
        baseHsv.copyInto(hsv)
        hsv[0] = (baseHsv[0] + HUE_SWING_DEGREES * sin(time * HUE_SPEED)).mod(FULL_TURN_DEGREES)
        val color = Color.HSVToColor(hsv)
        threadPaint.color = color
        threadPaint.alpha = threadAlpha
        sheetPaint.color = color
        sheetPaint.alpha = sheetAlpha
    }

    private companion object {
        const val HSV_SIZE = 3
        const val FLOATS_PER_THREAD = 4
        const val FULL_TURN_DEGREES = 360f

        /** Tonun seçili renkten en fazla ne kadar kaydığı (derece) ve kayma hızı (radyan/saniye). */
        const val HUE_SWING_DEGREES = 25f
        const val HUE_SPEED = 0.8f
    }
}
