package dev.sezertanci.drawingworkshop.domain.sound

import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Notaları 16 bit mono PCM örneklerine çevirir. Ses, temel frekans ile biraz üst oktavın toplamıdır;
 * böylece düz sinüsten daha canlı duyulur. Her nota hızla yükselip yumuşakça söner ve sıfırda biter,
 * bu yüzden notaların arasında çıtırtı olmaz.
 */
class SoundSynth(val sampleRate: Int = DEFAULT_SAMPLE_RATE) {

    fun render(tones: List<Tone>): ShortArray {
        val parts = tones.map(::renderTone)
        val result = ShortArray(parts.sumOf { it.size })
        var offset = 0
        for (part in parts) {
            part.copyInto(result, offset)
            offset += part.size
        }
        return result
    }

    private fun renderTone(tone: Tone): ShortArray {
        val count = tone.durationMs * sampleRate / MILLIS_PER_SECOND
        val attackCount = (ATTACK_MS * sampleRate / MILLIS_PER_SECOND).coerceAtMost(count)
        var phase = 0.0
        return ShortArray(count) { i ->
            val progress = i.toDouble() / count
            val frequency = tone.startHz + (tone.endHz - tone.startHz) * progress
            phase += TWO_PI * frequency / sampleRate
            val wave = (sin(phase) + OVERTONE_LEVEL * sin(2 * phase)) / (1 + OVERTONE_LEVEL)
            val attack = if (i < attackCount) i.toDouble() / attackCount else 1.0
            val release = (1 - progress) * (1 - progress)
            (wave * attack * release * tone.volume * Short.MAX_VALUE).roundToInt().toShort()
        }
    }

    companion object {
        const val DEFAULT_SAMPLE_RATE = 44_100
        private const val MILLIS_PER_SECOND = 1_000
        private const val ATTACK_MS = 5
        private const val OVERTONE_LEVEL = 0.25
        private const val TWO_PI = 2 * PI
    }
}
