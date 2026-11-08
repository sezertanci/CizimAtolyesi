package dev.sezertanci.drawingworkshop.domain.model

/** Renk panelindeki hazır renkler (ARGB). Çocuklar için canlı ve birbirinden kolay ayırt edilen tonlar seçildi. */
object Palette {
    const val WHITE: Int = 0xFFFFFFFF.toInt()
    const val BLACK: Int = 0xFF212121.toInt()
    const val RED: Int = 0xFFE53935.toInt()

    val colors: List<Int> = listOf(
        BLACK,
        0xFF757575.toInt(),
        WHITE,
        0xFF6D4C41.toInt(),
        0xFFA1887F.toInt(),
        0xFFFFCCBC.toInt(),
        RED,
        0xFFF06292.toInt(),
        0xFFF8BBD0.toInt(),
        0xFFFB8C00.toInt(),
        0xFFFFB74D.toInt(),
        0xFFFDD835.toInt(),
        0xFFFFF59D.toInt(),
        0xFF7CB342.toInt(),
        0xFF2E7D32.toInt(),
        0xFFAED581.toInt(),
        0xFF00ACC1.toInt(),
        0xFF80DEEA.toInt(),
        0xFF1E88E5.toInt(),
        0xFF0D47A1.toInt(),
        0xFF90CAF9.toInt(),
        0xFF8E24AA.toInt(),
        0xFF5E35B1.toInt(),
        0xFFCE93D8.toInt(),
    )
}
