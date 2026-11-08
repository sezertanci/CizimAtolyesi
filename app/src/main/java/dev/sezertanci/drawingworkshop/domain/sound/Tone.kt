package dev.sezertanci.drawingworkshop.domain.sound

/** Tek bir nota: frekansı başından sonuna doğru [startHz]'den [endHz]'e kayar. [volume] 0–1 arasıdır. */
data class Tone(val startHz: Double, val endHz: Double, val durationMs: Int, val volume: Double) {
    constructor(hz: Double, durationMs: Int, volume: Double) : this(hz, hz, durationMs, volume)
}
