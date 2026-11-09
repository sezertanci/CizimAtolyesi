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

/**
 * Kayıtlı katmanın başka boyuttaki bir tuvalde çizileceği dikdörtgen. Katmandaki içerik alanı (ör.
 * boyama sayfasının çizimi) her tuvale [fitCenter] ile yerleştiği için katman, eski ve yeni içerik
 * alanları üst üste gelecek şekilde ölçeklenip kaydırılır; böylece boya çizgilerle hizalı kalır.
 * İçerik boyutu verilmezse katmanın tamamı içerik sayılır ve sonuç [fitCenter] ile aynıdır.
 */
fun fitLayer(
    sourceWidth: Int,
    sourceHeight: Int,
    targetWidth: Int,
    targetHeight: Int,
    contentWidth: Int = sourceWidth,
    contentHeight: Int = sourceHeight,
): ScaledBounds {
    val before = fitCenter(contentWidth, contentHeight, sourceWidth, sourceHeight)
    val after = fitCenter(contentWidth, contentHeight, targetWidth, targetHeight)
    val scale = after.width / before.width
    return ScaledBounds(
        after.left - before.left * scale,
        after.top - before.top * scale,
        sourceWidth * scale,
        sourceHeight * scale,
    )
}
