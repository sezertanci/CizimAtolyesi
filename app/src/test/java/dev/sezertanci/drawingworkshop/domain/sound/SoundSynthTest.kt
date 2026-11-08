package dev.sezertanci.drawingworkshop.domain.sound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class SoundSynthTest {
    private val synth = SoundSynth(sampleRate = SAMPLE_RATE)

    @Test
    fun render_TwoTones_LengthIsSumOfDurations() {
        val samples = synth.render(listOf(Tone(HZ, 100, VOLUME), Tone(HZ, 50, VOLUME)))

        assertEquals(SAMPLE_RATE * 150 / 1_000, samples.size)
    }

    @Test
    fun render_Tone_StartsAndEndsSilentToAvoidClicks() {
        val samples = synth.render(listOf(Tone(HZ, 100, VOLUME)))

        assertEquals(0, samples.first().toInt())
        assertTrue(abs(samples.last().toInt()) < QUIET_LIMIT)
    }

    @Test
    fun render_Tone_PeakStaysWithinVolume() {
        val samples = synth.render(listOf(Tone(HZ, 100, VOLUME)))

        val peak = samples.maxOf { abs(it.toInt()) }
        assertTrue(peak <= VOLUME * Short.MAX_VALUE + 1)
        assertTrue(peak > VOLUME * Short.MAX_VALUE / 2)
    }

    @Test
    fun render_EveryEffect_ProducesSound() {
        SoundEffect.entries.forEach { effect ->
            assertTrue(effect.name, synth.render(effect.tones).any { it.toInt() != 0 })
        }
    }

    private companion object {
        const val SAMPLE_RATE = 8_000
        const val HZ = 440.0
        const val VOLUME = 0.5
        const val QUIET_LIMIT = 50
    }
}
