package dev.sezertanci.drawingworkshop.domain.sound

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavEncoderTest {

    @Test
    fun encode_Samples_WritesStandardHeader() {
        val bytes = WavEncoder.encode(shortArrayOf(1, -1, 300), SAMPLE_RATE)
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        assertEquals(WavEncoder.HEADER_SIZE + 6, bytes.size)
        assertEquals("RIFF", String(bytes, 0, 4))
        assertEquals(bytes.size - 8, buffer.getInt(4))
        assertEquals("WAVE", String(bytes, 8, 4))
        assertEquals(SAMPLE_RATE, buffer.getInt(24))
        assertEquals(16, buffer.getShort(34).toInt())
        assertEquals("data", String(bytes, 36, 4))
        assertEquals(6, buffer.getInt(40))
    }

    @Test
    fun encode_Samples_WritesLittleEndianData() {
        val bytes = WavEncoder.encode(shortArrayOf(300, -2), SAMPLE_RATE)
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        assertEquals(300, buffer.getShort(WavEncoder.HEADER_SIZE).toInt())
        assertEquals(-2, buffer.getShort(WavEncoder.HEADER_SIZE + 2).toInt())
    }

    private companion object {
        const val SAMPLE_RATE = 22_050
    }
}
