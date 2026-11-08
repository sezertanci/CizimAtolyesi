package dev.sezertanci.drawingworkshop.domain.sound

import java.nio.ByteBuffer
import java.nio.ByteOrder

/** PCM örneklerini 16 bit mono WAV dosyası biçimine çevirir (44 baytlık standart başlık + veri). */
object WavEncoder {
    const val HEADER_SIZE = 44
    private const val BYTES_PER_SAMPLE = 2
    private const val BITS_PER_SAMPLE = 16
    private const val CHANNELS = 1
    private const val FMT_CHUNK_SIZE = 16
    private const val PCM_FORMAT: Short = 1

    /** RIFF boyutu, dosyanın ilk 8 baytı ("RIFF" ve boyut alanı) dışındaki kısmıdır. */
    private const val RIFF_HEADER_REST = HEADER_SIZE - 8

    fun encode(samples: ShortArray, sampleRate: Int): ByteArray {
        val dataSize = samples.size * BYTES_PER_SAMPLE
        val buffer = ByteBuffer.allocate(HEADER_SIZE + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray()).putInt(RIFF_HEADER_REST + dataSize).put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray()).putInt(FMT_CHUNK_SIZE).putShort(PCM_FORMAT).putShort(CHANNELS.toShort())
        buffer.putInt(sampleRate).putInt(sampleRate * CHANNELS * BYTES_PER_SAMPLE)
        buffer.putShort((CHANNELS * BYTES_PER_SAMPLE).toShort()).putShort(BITS_PER_SAMPLE.toShort())
        buffer.put("data".toByteArray()).putInt(dataSize)
        samples.forEach(buffer::putShort)
        return buffer.array()
    }
}
