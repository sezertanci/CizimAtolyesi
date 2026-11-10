package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.graphics.Rect

/**
 * Parmak dursa bile her ekran karesinde kendiliğinden ilerleyen çizgi (ipek fırçası gibi).
 *
 * Böyle bir çizgi önizleme olarak tutulmaz; her karede yalnızca yeni parçaları doğrudan boya katmanına
 * çizilir. Böylece çizgi ne kadar uzarsa uzasın bir karenin maliyeti değişmez.
 */
interface LiveStroke {
    fun begin(x: Float, y: Float)

    /** Parmağın yeni konumu; çizgi bu noktaya doğru kendi hızıyla ilerler. */
    fun moveTo(x: Float, y: Float)

    /** Çizgiyi [seconds] kadar ilerletir ve yeni parçaların alanını döndürür; yeni parça yoksa `null`. */
    fun advance(seconds: Float): Rect?

    /** Son [advance] çağrısında oluşan parçaları çizer. Simetri kopyaları için birkaç kez çağrılabilir. */
    fun drawPending(canvas: Canvas)
}
