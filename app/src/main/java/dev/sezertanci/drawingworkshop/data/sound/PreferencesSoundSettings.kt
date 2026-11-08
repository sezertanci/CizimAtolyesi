package dev.sezertanci.drawingworkshop.data.sound

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sezertanci.drawingworkshop.domain.sound.SoundSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Ses ayarını uygulamanın kendi tercih dosyasında saklar; ilk açılışta ses açıktır. */
@Singleton
class PreferencesSoundSettings @Inject constructor(@ApplicationContext context: Context) : SoundSettings {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val _enabled = MutableStateFlow(preferences.getBoolean(KEY_ENABLED, true))

    override val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    override fun setEnabled(enabled: Boolean) {
        _enabled.value = enabled
        preferences.edit { putBoolean(KEY_ENABLED, enabled) }
    }

    private companion object {
        const val PREFERENCES_NAME = "settings"
        const val KEY_ENABLED = "sound_enabled"
    }
}
