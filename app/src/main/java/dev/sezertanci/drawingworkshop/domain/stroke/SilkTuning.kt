package dev.sezertanci.drawingworkshop.domain.stroke

/**
 * İpek tellerinin nasıl hareket ettiği.
 *
 * @property stiffness Telleri hedefe çeken yayın gücü; arttıkça teller parmağı daha yakından izler.
 * @property damping Salınımı söndüren sürtünme; azaldıkça teller hedefin çevresinde daha çok sallanır.
 * @property pace Hareketin hızı: dönme, dalgalanma ya da zikzak sıklığı (radyan/saniye).
 * @property wobbleShare Tellerin düzenli yerinden sapması, yayılma genişliğine oranla.
 */
data class SilkTuning(
    val stiffness: Float,
    val damping: Float,
    val pace: Float,
    val wobbleShare: Float,
)
