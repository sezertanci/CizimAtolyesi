package dev.sezertanci.drawingworkshop.data.coloring

/**
 * Hazır boyama sayfası. Sayfaların adı yoktur; galeride çizgileriyle görünür.
 *
 * @property id Kayıtlı resimlerde saklanan kalıcı kimlik; sonradan değiştirilmez.
 * @property art Çizgileri içeren vektör çizimin kaynak kimliği.
 */
data class ColoringPage(val id: String, val art: Int)
