package dev.sezertanci.drawingworkshop.domain.geometry

/**
 * Tuvalin içindeki görüntünün büyütme ve kaydırma durumu.
 *
 * Ekrandaki nokta = tuvaldeki nokta × [scale] + kaydırma. Büyütme 1 ile [maxScale] arasında kalır;
 * kaydırma, resmin kenarı tuvalin içine girip boşluk bırakmayacak şekilde sınırlanır.
 */
class Viewport(private val maxScale: Float) {
    private var width = 0f
    private var height = 0f

    var scale = MIN_SCALE
        private set
    var offsetX = 0f
        private set
    var offsetY = 0f
        private set

    /** Tuvalin boyutu değişince görüntü normal boyuta döner. */
    fun setSize(width: Int, height: Int) {
        this.width = width.toFloat()
        this.height = height.toFloat()
        scale = MIN_SCALE
        offsetX = 0f
        offsetY = 0f
    }

    /**
     * İki parmak hareketini uygular: [focusX], [focusY] noktası etrafında [scaleFactor] kadar büyütür,
     * ardından görüntüyü [panX], [panY] kadar kaydırır. Tüm değerler ekran pikselidir.
     */
    fun transform(focusX: Float, focusY: Float, scaleFactor: Float, panX: Float, panY: Float) {
        val newScale = (scale * scaleFactor).coerceIn(MIN_SCALE, maxScale)
        val applied = newScale / scale
        offsetX = clampOffset(focusX - (focusX - offsetX) * applied + panX, width, newScale)
        offsetY = clampOffset(focusY - (focusY - offsetY) * applied + panY, height, newScale)
        scale = newScale
    }

    fun toCanvasX(screenX: Float): Float = (screenX - offsetX) / scale

    fun toCanvasY(screenY: Float): Float = (screenY - offsetY) / scale

    private fun clampOffset(offset: Float, size: Float, newScale: Float): Float =
        offset.coerceIn(size - size * newScale, 0f)

    private companion object {
        const val MIN_SCALE = 1f
    }
}
