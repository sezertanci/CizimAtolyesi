package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import androidx.core.graphics.createBitmap
import dev.sezertanci.drawingworkshop.domain.fill.FillBounds

/**
 * Tuvalin boya katmanı: ekran dışında tutulan bir bitmap.
 *
 * Biten çizgiler bu bitmap'e bir kez işlenir; ekran her karede yalnızca bu hazır görüntüyü ve o an
 * çizilen çizgiyi gösterir. Böylece tuval ne kadar dolu olursa olsun çizim hızı değişmez.
 */
class DrawingSurface {
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private val replacePaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC) }

    val width: Int get() = bitmap?.width ?: 0
    val height: Int get() = bitmap?.height ?: 0

    /** Boyut değişirse yeni bitmap oluşturur ve eski çizimi sol üst köşeden aktarır. */
    fun resize(newWidth: Int, newHeight: Int) {
        if (newWidth <= 0 || newHeight <= 0 || (newWidth == width && newHeight == height)) return
        val resized = createBitmap(newWidth, newHeight)
        val resizedCanvas = Canvas(resized)
        bitmap?.let {
            resizedCanvas.drawBitmap(it, 0f, 0f, null)
            it.recycle()
        }
        bitmap = resized
        canvas = resizedCanvas
    }

    fun drawTo(target: Canvas) {
        bitmap?.let { target.drawBitmap(it, 0f, 0f, null) }
    }

    fun paint(block: (Canvas) -> Unit) {
        canvas?.let(block)
    }

    /** Verilen bölgenin kopyasını alır; bölge tuvalin dışındaysa `null` döner. */
    fun snapshot(region: Rect): RegionSnapshot? {
        val source = bitmap ?: return null
        val clipped = Rect(region)
        if (!clipped.intersect(0, 0, source.width, source.height)) return null
        val copy = Bitmap.createBitmap(source, clipped.left, clipped.top, clipped.width(), clipped.height())
        return RegionSnapshot(copy, clipped.left, clipped.top)
    }

    /** Kopyayı yerine koyar ve o bölgenin önceki halini ters işlem olarak döndürür. */
    fun restore(snapshot: RegionSnapshot): RegionSnapshot {
        val region = Rect(
            snapshot.left,
            snapshot.top,
            snapshot.left + snapshot.bitmap.width,
            snapshot.top + snapshot.bitmap.height,
        )
        val current = snapshot(region) ?: return snapshot
        canvas?.drawBitmap(snapshot.bitmap, snapshot.left.toFloat(), snapshot.top.toFloat(), replacePaint)
        snapshot.release()
        return current
    }

    fun readPixels(): IntArray {
        val source = bitmap ?: return IntArray(0)
        val pixels = IntArray(source.width * source.height)
        source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        return pixels
    }

    fun writePixels(pixels: IntArray, bounds: FillBounds) {
        val target = bitmap ?: return
        val offset = bounds.top * target.width + bounds.left
        target.setPixels(pixels, offset, target.width, bounds.left, bounds.top, bounds.width, bounds.height)
    }

    fun release() {
        bitmap?.recycle()
        bitmap = null
        canvas = null
    }
}
