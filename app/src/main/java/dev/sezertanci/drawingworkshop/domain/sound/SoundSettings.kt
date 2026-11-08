package dev.sezertanci.drawingworkshop.domain.sound

import kotlinx.coroutines.flow.StateFlow

/** Seslerin açık olup olmadığı. Varsayılan olarak açıktır ve seçim uygulama kapansa da hatırlanır. */
interface SoundSettings {
    val enabled: StateFlow<Boolean>

    fun setEnabled(enabled: Boolean)
}
