package dev.sezertanci.drawingworkshop.domain.model

/**
 * İpek şeridini oluşturan tellerin görünümü.
 *
 * @property threadCount Şeridi oluşturan tel sayısı.
 * @property threadWidthDp Tel izlerinin kalınlığı.
 * @property threadAlpha Tel izlerinin saydamlığı (0–255).
 * @property sheetAlpha Teller arasındaki ipek tabakasının saydamlığı (0–255).
 * @property spreadShare Şeridin genişliği, fırça boyutundan gelen genişliğe oranla.
 */
enum class SilkThreads(
    val threadCount: Int,
    val threadWidthDp: Float,
    val threadAlpha: Int,
    val sheetAlpha: Int,
    val spreadShare: Float,
) {
    /** İnce ve ayrık teller; hafif, uçuşan bir ipek. */
    SCATTERED(threadCount = 8, threadWidthDp = 1f, threadAlpha = 70, sheetAlpha = 28, spreadShare = 1f),

    /** Çok sayıda ince ve çok saydam tel; yoğun, yumuşak bir ipek tabakası. */
    TULLE(threadCount = 20, threadWidthDp = 0.6f, threadAlpha = 30, sheetAlpha = 14, spreadShare = 1f),

    /** Birbirine yakın, kalın ve parlak teller; dar ve dolgun bir demet. */
    DENSE(threadCount = 12, threadWidthDp = 2f, threadAlpha = 130, sheetAlpha = 40, spreadShare = 0.5f),
}
