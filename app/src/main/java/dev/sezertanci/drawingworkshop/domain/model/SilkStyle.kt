package dev.sezertanci.drawingworkshop.domain.model

import dev.sezertanci.drawingworkshop.domain.stroke.SilkMovement

/**
 * İpek fırçasının üç bağımsız seçimi; her biri ötekilerle birleşebilir.
 *
 * @property movement Tellerin parmağın çevresinde nasıl hareket ettiği.
 * @property tempo Tellerin ne kadar sıkı izlediği ve ne hızda hareket ettiği.
 * @property threads Tellerin sayısı, kalınlığı ve yoğunluğu.
 */
data class SilkStyle(
    val movement: SilkMovement = SilkMovement.SPIN,
    val tempo: SilkTempo = SilkTempo.WAVY,
    val threads: SilkThreads = SilkThreads.SCATTERED,
)
