package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Teller parmağın çevresinde dönen ve boyu nefes alır gibi uzayıp kısalan bir dizi oluşturur.
 * Parmak dursa da dizi dönmeyi sürdürür ve çember çizer.
 */
internal class SpinPattern(
    private val threadCount: Int,
    private val spread: Float,
    private val tuning: SilkTuning,
    random: Random,
) : SilkPattern {
    private val spinSpeed = tuning.pace * if (random.nextBoolean()) 1f else -1f
    private val phase = random.nextFloat() * FULL_TURN
    private var angle = random.nextFloat() * FULL_TURN

    override fun place(time: Float, headingX: Float, headingY: Float, offsets: FloatArray) {
        angle += spinSpeed * SilkMotion.STEP_SECONDS * (1f + SPIN_SWAY * sin(time * SWAY_SPEED + phase))
        val length = spread * (LENGTH_BASE + LENGTH_SWING * sin(time * BREATH_SPEED + phase))
        val wobble = spread * tuning.wobbleShare
        val directionX = cos(angle)
        val directionY = sin(angle)
        for (i in 0 until threadCount) {
            val across = wobble * sin(time * WOBBLE_SPEED + i * WOBBLE_STEP + phase)
            offsets.putOffset(i, length * threadPosition(i, threadCount), across, directionX, directionY)
        }
    }

    private companion object {
        /** Dönme hızının ne kadar ve hangi hızla dalgalandığı. */
        const val SPIN_SWAY = 0.6f
        const val SWAY_SPEED = 0.9f

        /** Dizinin yarı boyu, yayılma genişliğinin bu oranları arasında uzayıp kısalır. */
        const val LENGTH_BASE = 0.65f
        const val LENGTH_SWING = 0.35f
        const val BREATH_SPEED = 1.7f

        /** Tellerin dizi çizgisinden yana sapmasının hızı ve teller arasındaki faz farkı. */
        const val WOBBLE_SPEED = 5f
        const val WOBBLE_STEP = 0.9f
    }
}
