package dev.sezertanci.drawingworkshop.domain.sound

/** Ses efektlerini çalar. Ses kapalıysa sessiz kalır; çağıranın ayarı kontrol etmesi gerekmez. */
fun interface SoundPlayer {
    fun play(effect: SoundEffect)
}
