package dev.sezertanci.drawingworkshop.domain.sound

/** Uygulamadaki ses efektleri; her biri kodla üretilen kısa notalardan oluşur, hazır ses dosyası yoktur. */
enum class SoundEffect(val tones: List<Tone>) {
    /** Düğmeye dokununca kısa, yumuşak bir "pıt". */
    TAP(listOf(Tone(startHz = 880.0, endHz = 620.0, durationMs = 45, volume = 0.3))),

    /** Boya kovası doldurunca yukarı kayan bir "blup". */
    FILL(listOf(Tone(startHz = 260.0, endHz = 720.0, durationMs = 170, volume = 0.5))),

    /** Çıkartma resme yapışınca iki notalı "ding-ding". */
    STICKER(listOf(Tone(hz = 784.0, durationMs = 70, volume = 0.4), Tone(hz = 1175.0, durationMs = 130, volume = 0.4))),

    /** Resim galeriye kaydedilince çıkan kısa sevinç melodisi (do-mi-sol-do). */
    SUCCESS(
        listOf(
            Tone(hz = 523.0, durationMs = 90, volume = 0.4),
            Tone(hz = 659.0, durationMs = 90, volume = 0.4),
            Tone(hz = 784.0, durationMs = 90, volume = 0.4),
            Tone(hz = 1047.0, durationMs = 220, volume = 0.4),
        ),
    ),
}
