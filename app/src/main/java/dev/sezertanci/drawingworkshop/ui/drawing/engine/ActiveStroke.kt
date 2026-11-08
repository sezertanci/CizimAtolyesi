package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.graphics.Rect

/** Parmak ekrandayken çizilmekte olan çizgi. */
interface ActiveStroke {
    fun begin(x: Float, y: Float)

    fun extend(x: Float, y: Float)

    fun end()

    /** Çizgiyi ekranda boya katmanının üzerine önizleme olarak çizer. */
    fun drawPreview(canvas: Canvas)

    /** Çizgiyi kalıcı olarak boya katmanına işler. */
    fun commit(canvas: Canvas)

    /** Çizginin etkilediği alanı (kalınlık dahil) döndürür. */
    fun dirtyRegion(): Rect
}
