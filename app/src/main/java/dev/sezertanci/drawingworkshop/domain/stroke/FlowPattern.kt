package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.sin
import kotlin.random.Random

/**
 * Teller dönmeden, duman gibi parmağın izinden akar. Hareket yönüne dik bir yelpaze oluştururlar; yelpaze
 * yavaşça kapanıp parlak bir demette toplanır, sonra yeniden açılır. Her tel kendi hızında kıpırdar.
 * Parmak durunca teller olduğu yerde dalgalanmayı sürdürür ve bir bulut oluşturur.
 */
internal class FlowPattern(
    private val threadCount: Int,
    private val spread: Float,
    tuning: SilkTuning,
    random: Random,
) : SilkPattern {
    private val speed = tuning.pace * FLOW_SPEED
    private val drift = spread * (DRIFT_BASE + tuning.wobbleShare)
    private val phase = random.nextFloat() * FULL_TURN
    private val alongPhase = FloatArray(threadCount) { random.nextFloat() * FULL_TURN }
    private val acrossPhase = FloatArray(threadCount) { random.nextFloat() * FULL_TURN }
    private val rate = FloatArray(threadCount) { 1f + RATE_VARIATION * random.nextSigned() }

    override fun place(time: Float, headingX: Float, headingY: Float, offsets: FloatArray) {
        val opening = (1f + sin(time * speed * FAN_SPEED + phase)) / 2f
        val fan = spread * (FAN_CLOSED + FAN_SWING * opening)
        for (i in 0 until threadCount) {
            val turn = time * speed * rate[i]
            val along = drift * sin(turn + alongPhase[i])
            val across = fan * threadPosition(i, threadCount) + drift * sin(turn * CROSS_RATE + acrossPhase[i])
            offsets.putOffset(i, along, across, headingX, headingY)
        }
    }

    private companion object {
        /** Dalgalanmanın hızı, tempo hızına oranla. */
        const val FLOW_SPEED = 0.6f

        /** Tellerin kendi yerlerinden en az ne kadar kıpırdadığı, yayılma genişliğine oranla. */
        const val DRIFT_BASE = 0.1f

        /** Tellerin birbirinden ne kadar farklı hızda kıpırdadığı. */
        const val RATE_VARIATION = 0.4f

        /** Enine kıpırdamanın boyuna kıpırdamaya göre hızı; eşit olmayınca teller kıvrılır. */
        const val CROSS_RATE = 1.3f

        /** Yelpazenin kapalıyken ve açılırken genişliği, yayılma genişliğine oranla. */
        const val FAN_CLOSED = 0.1f
        const val FAN_SWING = 0.7f

        /** Yelpazenin açılıp kapanma hızı, dalgalanma hızına oranla. */
        const val FAN_SPEED = 0.35f
    }
}
