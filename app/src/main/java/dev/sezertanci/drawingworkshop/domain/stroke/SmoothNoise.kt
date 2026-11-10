package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.sin
import kotlin.random.Random

/**
 * Zamanla yavaşça ve düzensiz değişen, -1 ile 1 arasında bir değer. Farklı hızlardaki birkaç dalga
 * toplanır; hızlar birbirinin katı olmadığından desen kendini kolay kolay tekrarlamaz.
 */
internal class SmoothNoise(random: Random) {
    private val rates = FloatArray(BASE_RATES.size) { BASE_RATES[it] * (1f + RATE_VARIATION * random.nextSigned()) }
    private val phases = FloatArray(BASE_RATES.size) { random.nextFloat() * FULL_TURN }

    fun at(time: Float): Float {
        var sum = 0f
        for (i in rates.indices) sum += sin(time * rates[i] + phases[i])
        return sum / rates.size
    }

    private companion object {
        val BASE_RATES = floatArrayOf(1f, 1.7f, 2.9f)
        const val RATE_VARIATION = 0.2f
    }
}
