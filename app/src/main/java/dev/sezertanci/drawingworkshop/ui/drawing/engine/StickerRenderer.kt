package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import dev.sezertanci.drawingworkshop.domain.sticker.StickerControl
import dev.sezertanci.drawingworkshop.domain.sticker.StickerDraft
import dev.sezertanci.drawingworkshop.domain.sticker.StickerMetrics
import javax.inject.Inject

/**
 * Çıkartmayı ve düzenleme düğmelerini çizer. Çıkartma önizlemede ve resme işlenirken aynı metotla
 * çizilir; böylece ✓ sonrası görüntü değişmez. Düğmeler büyütme oranına bölünerek çizilir, ekranda
 * hep aynı boyda görünür.
 */
class StickerRenderer @Inject constructor(private val density: ScreenDensity) {
    val metrics = StickerMetrics(
        defaultSize = density.toPx(DEFAULT_SIZE_DP),
        minSize = density.toPx(MIN_SIZE_DP),
        maxSize = density.toPx(MAX_SIZE_DP),
        touchRadius = density.toPx(TOUCH_RADIUS_DP),
    )

    private val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ACCENT_COLOR
    }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val symbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeCap = Paint.Cap.ROUND
    }

    fun drawSticker(canvas: Canvas, draft: StickerDraft) {
        emojiPaint.textSize = draft.size
        val baseline = draft.centerY - (emojiPaint.ascent() + emojiPaint.descent()) / 2f
        canvas.drawText(draft.emoji, draft.centerX, baseline, emojiPaint)
    }

    /** Çıkartmanın kapladığı piksel alanı; geri alma için bu bölgenin kopyası saklanır. */
    fun bounds(draft: StickerDraft): Rect {
        emojiPaint.textSize = draft.size
        val halfWidth = emojiPaint.measureText(draft.emoji) / 2f
        val halfHeight = (emojiPaint.descent() - emojiPaint.ascent()) / 2f
        return RectF(
            draft.centerX - halfWidth,
            draft.centerY - halfHeight,
            draft.centerX + halfWidth,
            draft.centerY + halfHeight,
        ).toDirtyRegion(strokeWidth = 0f)
    }

    fun drawControls(canvas: Canvas, draft: StickerDraft, zoom: Float) {
        val half = draft.size / 2f
        framePaint.strokeWidth = density.toPx(FRAME_WIDTH_DP) / zoom
        canvas.drawRect(draft.centerX - half, draft.centerY - half, draft.centerX + half, draft.centerY + half, framePaint)
        val radius = density.toPx(BUTTON_RADIUS_DP) / zoom
        symbolPaint.strokeWidth = density.toPx(SYMBOL_WIDTH_DP) / zoom
        for (control in StickerControl.entries) {
            val x = draft.controlX(control)
            val y = draft.controlY(control)
            buttonPaint.color = control.color
            canvas.drawCircle(x, y, radius, buttonPaint)
            drawSymbol(canvas, control, x, y, radius * SYMBOL_SCALE)
        }
    }

    /** Düğmenin üzerindeki beyaz işaret: onayda tik, vazgeçte çarpı, tutamakta çift yönlü ok. */
    private fun drawSymbol(canvas: Canvas, control: StickerControl, x: Float, y: Float, r: Float) {
        when (control) {
            StickerControl.CONFIRM -> {
                val elbowX = x - r * CHECK_ELBOW_X
                val elbowY = y + r * CHECK_ELBOW_Y
                canvas.drawLine(x - r, y, elbowX, elbowY, symbolPaint)
                canvas.drawLine(elbowX, elbowY, x + r, y - r * CHECK_ELBOW_Y, symbolPaint)
            }
            StickerControl.CANCEL -> {
                canvas.drawLine(x - r, y - r, x + r, y + r, symbolPaint)
                canvas.drawLine(x - r, y + r, x + r, y - r, symbolPaint)
            }
            StickerControl.RESIZE -> {
                val head = r * ARROW_HEAD
                canvas.drawLine(x - r, y - r, x + r, y + r, symbolPaint)
                canvas.drawLine(x + r, y + r, x + r - head, y + r, symbolPaint)
                canvas.drawLine(x + r, y + r, x + r, y + r - head, symbolPaint)
                canvas.drawLine(x - r, y - r, x - r + head, y - r, symbolPaint)
                canvas.drawLine(x - r, y - r, x - r, y - r + head, symbolPaint)
            }
        }
    }

    private val StickerControl.color: Int
        get() = when (this) {
            StickerControl.CONFIRM -> CONFIRM_COLOR
            StickerControl.CANCEL -> CANCEL_COLOR
            StickerControl.RESIZE -> ACCENT_COLOR
        }

    private companion object {
        const val DEFAULT_SIZE_DP = 140f
        const val MIN_SIZE_DP = 56f
        const val MAX_SIZE_DP = 520f
        const val TOUCH_RADIUS_DP = 32f
        const val BUTTON_RADIUS_DP = 22f
        const val FRAME_WIDTH_DP = 2f
        const val SYMBOL_WIDTH_DP = 3f

        /** İşaretin, düğme yarıçapına oranı. */
        const val SYMBOL_SCALE = 0.45f

        /** Tikin kırılma noktası, işaret yarıçapına oranla. */
        const val CHECK_ELBOW_X = 0.3f
        const val CHECK_ELBOW_Y = 0.6f

        /** Ok ucunun uzunluğu, işaret yarıçapına oranla. */
        const val ARROW_HEAD = 0.8f

        const val ACCENT_COLOR = 0xFF1E88E5.toInt()
        const val CONFIRM_COLOR = 0xFF43A047.toInt()
        const val CANCEL_COLOR = 0xFFE53935.toInt()
    }
}
