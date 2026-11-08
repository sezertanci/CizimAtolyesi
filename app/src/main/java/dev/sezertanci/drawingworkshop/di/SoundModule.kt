package dev.sezertanci.drawingworkshop.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.sezertanci.drawingworkshop.data.sound.PreferencesSoundSettings
import dev.sezertanci.drawingworkshop.data.sound.SoundPoolPlayer
import dev.sezertanci.drawingworkshop.domain.sound.SoundPlayer
import dev.sezertanci.drawingworkshop.domain.sound.SoundSettings
import dev.sezertanci.drawingworkshop.domain.sound.SoundSynth

@Module
@InstallIn(SingletonComponent::class)
abstract class SoundModule {

    @Binds
    abstract fun bindSoundPlayer(impl: SoundPoolPlayer): SoundPlayer

    @Binds
    abstract fun bindSoundSettings(impl: PreferencesSoundSettings): SoundSettings

    companion object {
        @Provides
        fun provideSoundSynth(): SoundSynth = SoundSynth()
    }
}
