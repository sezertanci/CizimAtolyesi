package dev.sezertanci.drawingworkshop.domain.sticker

import kotlin.math.abs
import kotlin.math.max

/**
 * Çıkartma ölçüleri. Boyutlar büyütmesiz tuval pikselidir; [touchRadius] ise ekran pikselidir ve
 * büyütme oranına bölünerek kullanılır, böylece düğmeler her büyütmede aynı kolaylıkla yakalanır.
 */
data class StickerMetrics(val defaultSize: Float, val minSize: Float, val maxSize: Float, val touchRadius: Float)

/** Bir çıkartma dokunuşunun sonucu. */
enum class StickerTouchResult {
    EDITING,
    CONFIRMED,
    CANCELLED,
}

/**
 * Çıkartmanın düzenleme hâlini yönetir. Çıkartma yokken dokunulan yere yeni çıkartma konur.
 * Çıkartma varken: onay düğmesi [StickerTouchResult.CONFIRMED] döndürür (çıkartmayı resme işlemek
 * çağırana aittir), vazgeç düğmesi çıkartmayı kaldırır, sağ alt tutamaç boyutlandırır, çıkartmaya
 * dokunmak onu sürükler. Çıkartmanın dışına dokunulursa çıkartma o noktaya taşınır.
 */
class StickerEditor(private val metrics: StickerMetrics) {
    var draft: StickerDraft? = null
        private set

    private var drag = Drag.NONE
    private var lastX = 0f
    private var lastY = 0f

    /** [zoom] büyüdükçe yeni çıkartma resim üzerinde küçülür; ekranda hep aynı boyda konur. */
    fun touchStart(emoji: String, x: Float, y: Float, zoom: Float): StickerTouchResult {
        val current = draft
        if (current == null) {
            draft = StickerDraft(emoji, x, y, clampSize(metrics.defaultSize / zoom))
            startDrag(Drag.MOVE, x, y)
            return StickerTouchResult.EDITING
        }
        when (current.controlAt(x, y, metrics.touchRadius / zoom)) {
            StickerControl.CONFIRM -> return StickerTouchResult.CONFIRMED
            StickerControl.CANCEL -> {
                take()
                return StickerTouchResult.CANCELLED
            }
            StickerControl.RESIZE -> startDrag(Drag.RESIZE, x, y)
            null -> {
                if (!current.contains(x, y)) draft = current.copy(centerX = x, centerY = y)
                startDrag(Drag.MOVE, x, y)
            }
        }
        return StickerTouchResult.EDITING
    }

    fun touchMove(x: Float, y: Float) {
        val current = draft ?: return
        draft = when (drag) {
            Drag.MOVE -> current.copy(centerX = current.centerX + x - lastX, centerY = current.centerY + y - lastY)
            Drag.RESIZE -> current.copy(size = clampSize(2f * max(abs(x - current.centerX), abs(y - current.centerY))))
            Drag.NONE -> return
        }
        lastX = x
        lastY = y
    }

    fun touchEnd() {
        drag = Drag.NONE
    }

    /** Düzenlenen çıkartmanın yerini ve boyunu koruyarak resmini değiştirir. */
    fun changeEmoji(emoji: String) {
        draft = draft?.copy(emoji = emoji)
    }

    /** Düzenlenen çıkartmayı döndürür ve düzenleme hâlini kapatır. */
    fun take(): StickerDraft? {
        val current = draft
        draft = null
        drag = Drag.NONE
        return current
    }

    private fun startDrag(mode: Drag, x: Float, y: Float) {
        drag = mode
        lastX = x
        lastY = y
    }

    private fun clampSize(size: Float): Float = size.coerceIn(metrics.minSize, metrics.maxSize)

    private enum class Drag { NONE, MOVE, RESIZE }
}
