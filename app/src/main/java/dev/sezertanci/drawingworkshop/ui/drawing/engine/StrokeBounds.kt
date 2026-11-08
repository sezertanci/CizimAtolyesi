package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.ceil

private const val EDGE_PADDING = 2f

/** Yolun sınırlarını, çizgi kalınlığının yarısı ve kenar yumuşatması kadar genişleterek piksel alanına çevirir. */
internal fun RectF.toDirtyRegion(strokeWidth: Float): Rect {
    val padding = strokeWidth / 2f + EDGE_PADDING
    return Rect(
        (left - padding).toInt(),
        (top - padding).toInt(),
        ceil(right + padding).toInt(),
        ceil(bottom + padding).toInt(),
    )
}
