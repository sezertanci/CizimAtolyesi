package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.PI
import kotlin.random.Random

/** Tellerin parmağa göre nerede durmak istediğini hesaplar. Teller bu hedeflere yaylarla çekilir. */
internal interface SilkPattern {
    /** Yayların tempoya göre kaç kat sıkı olduğu; sıkı yaylar keskin köşeleri yumuşatmadan izler. */
    val grip: Float get() = 1f

    /**
     * Her hareket adımında bir kez çağrılır. Her telin parmağa göre hedef uzaklığını [offsets] dizisine
     * x, y sırasıyla yazar. [stillSeconds] parmağın ne kadar süredir yerinde durduğudur; [headingX],
     * [headingY] parmağın son hareket yönüdür (birim vektör).
     */
    fun place(time: Float, stillSeconds: Float, headingX: Float, headingY: Float, offsets: FloatArray)

    companion object {
        fun create(movement: SilkMovement, threadCount: Int, spread: Float, tuning: SilkTuning, random: Random) =
            when (movement) {
                SilkMovement.SPIN -> SpinPattern(threadCount, spread, tuning, random)
                SilkMovement.FLOW -> FlowPattern(threadCount, spread, tuning, random)
                SilkMovement.ZIGZAG -> ZigzagPattern(threadCount, spread, tuning, random)
                SilkMovement.SMOKE -> SmokePattern(threadCount, spread, tuning, random)
            }
    }
}

internal val FULL_TURN = (2 * PI).toFloat()

/** Telin dizideki yeri: bir uçta -1, ortada 0, öbür uçta 1. */
internal fun threadPosition(thread: Int, threadCount: Int): Float = 2f * thread / (threadCount - 1) - 1f

/** [-1, 1] arasında rastgele bir sayı. */
internal fun Random.nextSigned(): Float = nextFloat() * 2f - 1f

/** Yönü ([directionX], [directionY]) olan bir eksene göre boyuna ve enine uzaklığı x, y olarak yazar. */
internal fun FloatArray.putOffset(thread: Int, along: Float, across: Float, directionX: Float, directionY: Float) {
    this[2 * thread] = along * directionX - across * directionY
    this[2 * thread + 1] = along * directionY + across * directionX
}
