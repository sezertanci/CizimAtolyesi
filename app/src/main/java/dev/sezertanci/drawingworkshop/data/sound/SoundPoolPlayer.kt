package dev.sezertanci.drawingworkshop.data.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sezertanci.drawingworkshop.di.ApplicationScope
import dev.sezertanci.drawingworkshop.di.IoDispatcher
import dev.sezertanci.drawingworkshop.domain.sound.SoundEffect
import dev.sezertanci.drawingworkshop.domain.sound.SoundPlayer
import dev.sezertanci.drawingworkshop.domain.sound.SoundSettings
import dev.sezertanci.drawingworkshop.domain.sound.SoundSynth
import dev.sezertanci.drawingworkshop.domain.sound.WavEncoder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sesleri açılışta bir kez üretir, önbellek klasörüne WAV olarak yazar ve [SoundPool]'a yükler.
 * Hazırlık arka planda yapılır; hazır olmayan ses o an çalınmaz, uygulama beklemez.
 * SoundPool çalma işini kendi iş parçacığında yaptığı için [play] arayüzü dondurmaz.
 */
@Singleton
class SoundPoolPlayer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settings: SoundSettings,
    private val synth: SoundSynth,
    @ApplicationScope scope: CoroutineScope,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
) : SoundPlayer {
    private val pool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val soundIds = ConcurrentHashMap<SoundEffect, Int>()

    init {
        scope.launch(ioDispatcher) { SoundEffect.entries.forEach(::load) }
    }

    override fun play(effect: SoundEffect) {
        if (!settings.enabled.value) return
        val soundId = soundIds[effect] ?: return
        pool.play(soundId, FULL_VOLUME, FULL_VOLUME, PRIORITY, NO_LOOP, NORMAL_RATE)
    }

    private fun load(effect: SoundEffect) {
        val wav = WavEncoder.encode(synth.render(effect.tones), synth.sampleRate)
        val file = File(context.cacheDir, "sound_${effect.name.lowercase()}.wav").apply { writeBytes(wav) }
        soundIds[effect] = pool.load(file.path, PRIORITY)
    }

    private companion object {
        const val MAX_STREAMS = 4
        const val FULL_VOLUME = 1f
        const val PRIORITY = 1
        const val NO_LOOP = 0
        const val NORMAL_RATE = 1f
    }
}
