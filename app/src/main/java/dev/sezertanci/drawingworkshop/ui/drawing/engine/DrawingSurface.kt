package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.Size
import androidx.core.graphics.createBitmap
import dev.sezertanci.drawingworkshop.domain.geometry.PixelRect
import dev.sezertanci.drawingworkshop.domain.geometry.fitLayer

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

    /**
     * Katmandaki içerik alanının (boyama sayfasının çizimi) en-boy boyutu. Katman başka boyuttaki bir
     * tuvale aktarılırken bu alan çizgilerle hizalı kalacak şekilde ölçeklenir; `null` ise katmanın tamamı.
     */
    var contentSize: Size? = null

    /**
     * Boyut değişirse yeni bitmap oluşturur ve eski çizimi yeni tuvale hizalayarak aktarır. Eski çizim
     * ölçeklendiyse `true` döner; o zaman eski koordinatlardaki geri al kayıtları geçersizdir.
     */
    fun resize(newWidth: Int, newHeight: Int): Boolean {
        if (newWidth <= 0 || newHeight <= 0 || (newWidth == width && newHeight == height)) return false
        val previous = bitmap
        val resized = createBitmap(newWidth, newHeight)
        val resizedCanvas = Canvas(resized)
        previous?.let {
            resizedCanvas.drawAligned(it)
            it.recycle()
        }
        bitmap = resized
        canvas = resizedCanvas
        applyPending()
        return previous != null
    }

    /**
     * Kayıtlı boya katmanını yükler; tuval henüz boyutlanmadıysa boyut belli olunca uygulanır.
     * Boyut farklıysa [contentSize] alanı çizgilerle hizalanarak yerleştirilir. [source] bu sınıfa
     * devredilir ve sonra serbest bırakılır.
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
        target.clearPixels()
        target.drawAligned(source)
        source.recycle()
    }

    /** [source] katmanını bu tuvalin boyutuna, içerik alanı çizgilerle hizalı kalacak şekilde çizer. */
    private fun Canvas.drawAligned(source: Bitmap) {
        if (source.width == this.width && source.height == this.height) {
            drawBitmap(source, 0f, 0f, null)
            return
        }
        val content = contentSize ?: Size(source.width, source.height)
        val bounds = fitLayer(source.width, source.height, this.width, this.height, content.width, content.height)
        val destination = RectF(bounds.left, bounds.top, bounds.left + bounds.width, bounds.top + bounds.height)
        drawBitmap(source, null, destination, scalePaint)
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
    fun snapshot(region: Rect): RegionSnapshot? {
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

/** Tuvalin tüm piksellerini saydam yapar. */
fun Canvas.clearPixels() {
    drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
}
