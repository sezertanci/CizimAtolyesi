package dev.sezertanci.drawingworkshop.domain.sticker

import kotlin.math.abs
import kotlin.math.hypot

/** Düzenlenen çıkartmanın köşelerindeki düğmeler. */
enum class StickerControl {
    CONFIRM,
    CANCEL,
    RESIZE,
}

/**
 * Tuvale henüz işlenmemiş, düzenlenen çıkartma. Konum ve [size] tuvalin pikselidir; [size],
 * çıkartmanın kare çerçevesinin kenar uzunluğudur.
 */
data class StickerDraft(val emoji: String, val centerX: Float, val centerY: Float, val size: Float) {
    private val half: Float get() = size / 2f

    /** Vazgeç düğmesi sol üst, onay düğmesi sağ üst, boyutlandırma tutamacı sağ alt köşededir. */
    fun controlX(control: StickerControl): Float =
        if (control == StickerControl.CANCEL) centerX - half else centerX + half

    fun controlY(control: StickerControl): Float =
        if (control == StickerControl.RESIZE) centerY + half else centerY - half

    /** [radius] içinde kalan düğmeyi döndürür; dokunulan yerde düğme yoksa `null`. */
    fun controlAt(x: Float, y: Float, radius: Float): StickerControl? =
        StickerControl.entries.firstOrNull { hypot(x - controlX(it), y - controlY(it)) <= radius }

    fun contains(x: Float, y: Float): Boolean = abs(x - centerX) <= half && abs(y - centerY) <= half
}
