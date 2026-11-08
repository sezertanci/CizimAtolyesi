package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Gökkuşağı fırçasının rengini, çizginin o ana kadar aldığı yola göre hesaplar.
 *
 * Renk kırmızıdan başlayıp turuncu, sarı, yeşil, mavi ve mordan geçerek [cycleLength] yolda yeniden
 * kırmızıya döner. Renk tonu [STEPS] kademeye bölünür: aynı kademedeki parçalar tek yol olarak
 * çizilebilsin diye ton her küçük harekette değil, kademe değişince değişir.
 */
class RainbowCycle(private val cycleLength: Float) {
    /** [distance] yoldaki kademenin sırası (0 ile [STEPS] - 1 arası). */
    fun stepAt(distance: Float): Int {
        val turn = (distance / cycleLength).mod(1f)
        return (turn * STEPS).toInt().coerceAtMost(STEPS - 1)
    }

    companion object {
        const val STEPS = 48
        private const val FULL_TURN_DEGREES = 360f
        private const val HUE_SECTOR_DEGREES = 60f
        private const val SECTOR_COUNT = 6
        private const val CHANNEL_MAX = 255
        private const val OPAQUE_ALPHA = 0xFF shl 24
        private const val RED_SHIFT = 16
        private const val GREEN_SHIFT = 8

        /** Kademenin tam doygun ve parlak rengi (ARGB). */
        fun colorOf(step: Int): Int = hueToColor(step * FULL_TURN_DEGREES / STEPS)

        private fun hueToColor(hue: Float): Int {
            val sector = hue / HUE_SECTOR_DEGREES
            val rising = 1f - abs(sector.mod(2f) - 1f)
            val (r, g, b) = when (sector.toInt() % SECTOR_COUNT) {
                0 -> Triple(1f, rising, 0f)
                1 -> Triple(rising, 1f, 0f)
                2 -> Triple(0f, 1f, rising)
                3 -> Triple(0f, rising, 1f)
                4 -> Triple(rising, 0f, 1f)
                else -> Triple(1f, 0f, rising)
            }
            return OPAQUE_ALPHA or (channel(r) shl RED_SHIFT) or (channel(g) shl GREEN_SHIFT) or channel(b)
        }

        private fun channel(value: Float): Int = (value * CHANNEL_MAX).roundToInt()
    }
}
