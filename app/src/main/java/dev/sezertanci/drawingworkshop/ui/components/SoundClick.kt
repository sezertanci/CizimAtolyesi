package dev.sezertanci.drawingworkshop.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import dev.sezertanci.drawingworkshop.domain.sound.SoundEffect
import dev.sezertanci.drawingworkshop.domain.sound.SoundPlayer

/** Ekranlara verilen ses çalıcı. Verilmezse (ör. önizlemede) sessiz kalır. */
val LocalSoundPlayer = staticCompositionLocalOf { SoundPlayer {} }

/** Dokununca kısa bir tıklama sesi çalan düğme davranışı. Tüm düğmeler ve kartlar bunu kullanır. */
@Composable
fun Modifier.clickableWithSound(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val sounds = LocalSoundPlayer.current
    return clickable(enabled = enabled, role = Role.Button) {
        sounds.play(SoundEffect.TAP)
        onClick()
    }
}
