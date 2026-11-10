package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Rect

/**
 * Bir [LiveStroke] çizgisini, sürdüğü boyunca boya katmanına işler ve tek adımda geri alınabilir tutar.
 *
 * Boya katmanı sabit boyutlu karolara bölünür. Bir karoya ilk kez çizilmeden önce kopyası alınır; çizgi
 * bitince bu kopyalar tek bir geri al kaydı olur. Böylece kayıt, yalnızca çizginin geçtiği yerleri tutar.
 * Çizgi iptal edilirse kopyalar yerine konarak katman çizgiden önceki hâline döner.
 */
class LiveStrokeSession(
    private val stroke: LiveStroke,
    private val painter: SymmetryPainter,
    private val surface: DrawingSurface,
) {
    private val tiles = HashMap<Int, RegionSnapshot>()
    private var lastFrameNanos = NO_FRAME

    /** Parmak kalktıysa `true`; çizgi sönerek sürer ve artık parmağı izlemez. */
    var isReleased = false
        private set

    val isFinished: Boolean get() = stroke.isFinished

    fun moveTo(x: Float, y: Float) = stroke.moveTo(x, y)

    fun release() {
        isReleased = true
        stroke.release()
    }

    /** Ekran karesinin zamanına göre çizgiyi ilerletir; katmana yeni bir şey çizildiyse `true` döner. */
    fun advance(frameTimeNanos: Long): Boolean {
        val seconds = if (lastFrameNanos == NO_FRAME) 0f else (frameTimeNanos - lastFrameNanos) / NANOS_PER_SECOND
        lastFrameNanos = frameTimeNanos
        val region = stroke.advance(seconds) ?: return false
        painter.regions(region).forEach(::captureTiles)
        surface.paint { painter.drawCopies(it, stroke::drawPending) }
        return true
    }

    /** Çizginin geri al kaydı; katmana hiçbir şey çizilmediyse `null`. */
    fun finish(): LayerChange? = tiles.values.toList().takeIf { it.isNotEmpty() }?.let(::LayerChange)

    /** Katmanı çizgiden önceki hâline döndürür. */
    fun cancel() {
        finish()?.let { surface.restore(it).release() }
    }

    /** Bölgenin değdiği karolardan kopyası henüz alınmamış olanları kopyalar. */
    private fun captureTiles(region: Rect) {
        val clipped = Rect(region)
        if (!clipped.intersect(0, 0, surface.width, surface.height)) return
        val columns = (surface.width + TILE_SIZE - 1) / TILE_SIZE
        for (row in clipped.top / TILE_SIZE..(clipped.bottom - 1) / TILE_SIZE) {
            for (column in clipped.left / TILE_SIZE..(clipped.right - 1) / TILE_SIZE) {
                val key = row * columns + column
                if (key !in tiles) snapshotTile(row, column)?.let { tiles[key] = it }
            }
        }
    }

    private fun snapshotTile(row: Int, column: Int): RegionSnapshot? {
        val left = column * TILE_SIZE
        val top = row * TILE_SIZE
        return surface.snapshot(Rect(left, top, left + TILE_SIZE, top + TILE_SIZE))
    }

    private companion object {
        const val NO_FRAME = -1L
        const val NANOS_PER_SECOND = 1_000_000_000f

        /** Geri al kopyası alınan karoların kenar uzunluğu (piksel). */
        const val TILE_SIZE = 128
    }
}
