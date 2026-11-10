package dev.sezertanci.drawingworkshop.domain.stroke

/**
 * İpek tellerinin nasıl hareket ettiği.
 *
 * @property stiffness Telleri hedefe çeken yayın gücü; arttıkça teller parmağı daha yakından izler.
 * @property damping Salınımı söndüren sürtünme; azaldıkça teller hedefin çevresinde daha çok sallanır.
 * @property spinSpeed Tel dizisinin dönme hızı (radyan/saniye).
 * @property wobbleShare Tellerin dizi çizgisinden yana sapması, yayılma genişliğine oranla.
 */
data class SilkTuning(
    val stiffness: Float,
    val damping: Float,
    val spinSpeed: Float,
    val wobbleShare: Float,
)
