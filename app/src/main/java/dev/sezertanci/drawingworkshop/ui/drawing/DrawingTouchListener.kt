package dev.sezertanci.drawingworkshop.ui.drawing

/** Tuval görünümünün dokunma olaylarını ilettiği arayüz. Koordinatlar tuvalin piksel koordinatlarıdır. */
interface DrawingTouchListener {
    /** [zoom], görüntünün o anki büyütme oranıdır; fırça ekranda hep aynı kalınlıkta görünsün diye kullanılır. */
    fun onTouchStart(x: Float, y: Float, zoom: Float)

    fun onTouchMove(x: Float, y: Float)

    /** Bir dokunma olayındaki tüm noktalar iletildikten sonra bir kez çağrılır. */
    fun onTouchBatchEnd()

    fun onTouchEnd()

    fun onTouchCancel()
}
