package dev.sezertanci.drawingworkshop.domain.model

import dev.sezertanci.drawingworkshop.domain.stroke.SilkTuning

/**
 * İpek fırçasının hazır stilleri.
 *
 * @property threadCount Şeridi oluşturan tel sayısı.
 * @property tuning Tellerin hareketi.
 * @property threadWidthDp Tel izlerinin kalınlığı.
 * @property threadAlpha Tel izlerinin saydamlığı (0–255).
 * @property sheetAlpha Teller arasındaki ipek tabakasının saydamlığı (0–255).
 */
enum class SilkStyle(
    val threadCount: Int,
    val tuning: SilkTuning,
    val threadWidthDp: Float,
    val threadAlpha: Int,
    val sheetAlpha: Int,
) {
    /** Az tel; yavaş döner ve parmağı yakından izler. */
    CALM(
        threadCount = 5,
        tuning = SilkTuning(stiffness = 260f, damping = 24f, spinSpeed = 1.2f, wobbleShare = 0.1f),
        threadWidthDp = 1f,
        threadAlpha = 80,
        sheetAlpha = 30,
    ),

    /** Dengeli: kıvrılarak ve hafifçe sallanarak izler. */
    WAVY(
        threadCount = 8,
        tuning = SilkTuning(stiffness = 110f, damping = 11f, spinSpeed = 2.4f, wobbleShare = 0.25f),
        threadWidthDp = 1f,
        threadAlpha = 70,
        sheetAlpha = 28,
    ),

    /** Çok tel; hızlı döner, çok geride kalır ve savrulur. */
    STORM(
        threadCount = 14,
        tuning = SilkTuning(stiffness = 45f, damping = 5f, spinSpeed = 5f, wobbleShare = 0.45f),
        threadWidthDp = 1f,
        threadAlpha = 55,
        sheetAlpha = 18,
    ),

    /** Çok sayıda ince ve çok saydam tel; yoğun, yumuşak bir ipek tabakası bırakır. */
    TULLE(
        threadCount = 20,
        tuning = SilkTuning(stiffness = 130f, damping = 14f, spinSpeed = 1.6f, wobbleShare = 0.15f),
        threadWidthDp = 0.6f,
        threadAlpha = 30,
        sheetAlpha = 14,
    ),
    ;

    companion object {
        /** Yeni çizimde seçili gelen stil. */
        val DEFAULT = WAVY
    }
}
