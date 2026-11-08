package dev.sezertanci.drawingworkshop.ui.drawing

/** Tuval görünümünün dokunma olaylarını ilettiği arayüz. Koordinatlar tuvalin piksel koordinatlarıdır. */
interface DrawingTouchListener {
    fun onTouchStart(x: Float, y: Float)

    fun onTouchMove(x: Float, y: Float)

    /** Bir dokunma olayındaki tüm noktalar iletildikten sonra bir kez çağrılır. */
    fun onTouchBatchEnd()

    fun onTouchEnd()

    fun onTouchCancel()
}
