package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import dev.sezertanci.drawingworkshop.domain.geometry.PixelRect
import dev.sezertanci.drawingworkshop.domain.geometry.fitCenter

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
    private val scalePaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private var pending: Bitmap? = null

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
        applyPending()
    }

    /**
     * Kayıtlı boya katmanını yükler; tuval henüz boyutlanmadıysa boyut belli olunca uygulanır.
     * Görüntü oranı korunarak tuvale sığdırılır. [source] bu sınıfa devredilir ve sonra serbest bırakılır.
     */
    fun load(source: Bitmap) {
        pending?.recycle()
        pending = source
        applyPending()
    }

    /** Boya katmanının kopyası; kayıt ve dışa aktarma için. Serbest bırakmak çağırana aittir. */
    fun copy(): Bitmap? = bitmap?.copy(Bitmap.Config.ARGB_8888, false)

    private fun applyPending() {
        val source = pending ?: return
        val target = canvas ?: return
        pending = null
        target.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        if (source.width == width && source.height == height) {
            target.drawBitmap(source, 0f, 0f, null)
        } else {
            val bounds = fitCenter(source.width, source.height, width, height)
            val destination = RectF(bounds.left, bounds.top, bounds.left + bounds.width, bounds.top + bounds.height)
            target.drawBitmap(source, null, destination, scalePaint)
        }
        source.recycle()
    }

    fun drawTo(target: Canvas) {
        bitmap?.let { target.drawBitmap(it, 0f, 0f, null) }
    }

    fun paint(block: (Canvas) -> Unit) {
        canvas?.let(block)
    }

    /** Bölgelerin kopyasını tek geri al kaydı olarak alır; hepsi tuvalin dışındaysa `null` döner. */
    fun snapshot(regions: List<Rect>): LayerChange? =
        regions.mapNotNull(::snapshot).takeIf { it.isNotEmpty() }?.let(::LayerChange)

    /** Kopyaları yerine koyar ve önceki hâli ters işlem olarak döndürür. Bölgeler kesişmediği için sıra önemsizdir. */
    fun restore(change: LayerChange): LayerChange = LayerChange(change.snapshots.map(::restore))

    /** Verilen bölgenin kopyasını alır; bölge tuvalin dışındaysa `null` döner. */
    private fun snapshot(region: Rect): RegionSnapshot? {
        val source = bitmap ?: return null
        val clipped = Rect(region)
        if (!clipped.intersect(0, 0, source.width, source.height)) return null
        val copy = Bitmap.createBitmap(source, clipped.left, clipped.top, clipped.width(), clipped.height())
        return RegionSnapshot(copy, clipped.left, clipped.top)
    }

    /** Kopyayı yerine koyar ve o bölgenin önceki halini ters işlem olarak döndürür. */
    private fun restore(snapshot: RegionSnapshot): RegionSnapshot {
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

    fun writePixels(pixels: IntArray, bounds: PixelRect) {
        val target = bitmap ?: return
        val offset = bounds.top * target.width + bounds.left
        target.setPixels(pixels, offset, target.width, bounds.left, bounds.top, bounds.width, bounds.height)
    }

    fun release() {
        pending?.recycle()
        pending = null
        bitmap?.recycle()
        bitmap = null
        canvas = null
    }
}
