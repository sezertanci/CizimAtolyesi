package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.abs
import kotlin.math.floor
import kotlin.random.Random

/**
 * Teller hareket yönüne dik olarak bir uçtan öbür uca düz çizgilerle savrulur; parmak ilerledikçe kırık
 * çizgiler çizilir. Gevşek tempolarda teller birbirinin biraz gerisinden gelir ve örgü gibi görünür.
 */
internal class ZigzagPattern(
    private val threadCount: Int,
    spread: Float,
    private val tuning: SilkTuning,
    random: Random,
) : SilkPattern {
    private val turnsPerSecond = tuning.pace * ZIGZAG_RATE
    private val swing = spread * SWING_SHARE
    private val ribbon = spread * RIBBON_SHARE
    private val phase = random.nextFloat()

    override val grip = GRIP

    override fun place(time: Float, stillSeconds: Float, headingX: Float, headingY: Float, offsets: FloatArray) {
        for (i in 0 until threadCount) {
            val position = threadPosition(i, threadCount)
            val lag = position * LAG_SHARE * tuning.wobbleShare
            val across = swing * triangle(time * turnsPerSecond + phase - lag) + ribbon * position
            offsets.putOffset(i, 0f, across, headingX, headingY)
        }
    }

    /** Üçgen dalga: her turda 1'den -1'e ve yeniden 1'e düz çizgilerle gider. */
    private fun triangle(turns: Float): Float = 4f * abs(turns - floor(turns) - 0.5f) - 1f

    private companion object {
        /** Saniyedeki zikzak sayısı, tempo hızına oranla. */
        const val ZIGZAG_RATE = 0.5f

        /** Savrulmanın genişliği ve tellerin yan yana durduğu şeridin genişliği, yayılma genişliğine oranla. */
        const val SWING_SHARE = 0.8f
        const val RIBBON_SHARE = 0.25f

        /** Dizinin uçlarındaki tellerin ortadakilerin ne kadar gerisinden geldiği (tur). */
        const val LAG_SHARE = 0.2f

        /** Köşeler sivri kalsın diye yaylar bu kat sıkıdır. */
        const val GRIP = 5f
    }
}
