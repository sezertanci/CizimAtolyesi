package dev.sezertanci.drawingworkshop.domain.model

import dev.sezertanci.drawingworkshop.domain.stroke.SilkTuning

/** İpek tellerinin hareket temposu. */
enum class SilkTempo(val tuning: SilkTuning) {
    /** Yavaş hareket eder ve parmağı yakından izler. */
    CALM(SilkTuning(stiffness = 260f, damping = 24f, pace = 1.2f, wobbleShare = 0.1f)),

    /** Dengeli: kıvrılarak ve hafifçe sallanarak izler. */
    WAVY(SilkTuning(stiffness = 110f, damping = 11f, pace = 2.4f, wobbleShare = 0.25f)),

    /** Hızlı hareket eder, çok geride kalır ve savrulur. */
    STORM(SilkTuning(stiffness = 45f, damping = 5f, pace = 5f, wobbleShare = 0.45f)),
}
