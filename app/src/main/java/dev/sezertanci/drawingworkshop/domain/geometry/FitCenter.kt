package dev.sezertanci.drawingworkshop.domain.geometry

import kotlin.math.min

/** Bir görüntünün hedef alana yerleşeceği dikdörtgen. */
data class ScaledBounds(val left: Float, val top: Float, val width: Float, val height: Float)

/**
 * Kaynağı en-boy oranını koruyarak hedefin içine sığdırır ve ortalar. Kaydedilen resim farklı
 * boyutta bir tuvalde açıldığında bozulmadan ve kesilmeden görünür.
 */
fun fitCenter(sourceWidth: Int, sourceHeight: Int, targetWidth: Int, targetHeight: Int): ScaledBounds {
    val scale = min(targetWidth.toFloat() / sourceWidth, targetHeight.toFloat() / sourceHeight)
    val width = sourceWidth * scale
    val height = sourceHeight * scale
    return ScaledBounds((targetWidth - width) / 2f, (targetHeight - height) / 2f, width, height)
}
