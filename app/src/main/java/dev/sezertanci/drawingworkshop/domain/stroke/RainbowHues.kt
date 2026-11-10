package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Gökkuşağı fırçasının rengini, çizginin o ana kadar aldığı yola göre hesaplar.
 *
 * Her çizgi rastgele bir renk tonuyla başlar. Her [legLength] yolda ton, rastgele yönde ve rastgele
 * büyüklükte dönerek bir sonraki hedef tona ulaşır. Geçiş yavaş başlayıp yavaş biter; böylece renkler
 * her çizgide başka bir sırayla, kesintisiz akarak değişir.
 */
class RainbowHues(private val legLength: Float, private val random: Random = Random.Default) {
    // Hedef tonlar 0–360 aralığına sıkıştırılmaz; böylece ara tonlar hep kısa yoldan ilerler.
    private val targets = mutableListOf(random.nextFloat() * FULL_TURN_DEGREES)

    /** [distance] yoldaki renk tonu, derece cinsinden (0 ile 360 arası). */
    fun hueAt(distance: Float): Float {
        val position = distance.coerceAtLeast(0f) / legLength
        val leg = position.toInt()
        while (targets.size <= leg + 1) targets.add(nextTarget(targets.last()))
        val from = targets[leg]
        val hue = from + (targets[leg + 1] - from) * smoothStep(position - leg)
        return hue.mod(FULL_TURN_DEGREES)
    }

    /** [distance] yoldaki tam doygun ve parlak renk (ARGB). */
    fun colorAt(distance: Float): Int = colorOf(hueAt(distance))

    private fun nextTarget(from: Float): Float {
        val turn = MIN_TURN_DEGREES + random.nextFloat() * (MAX_TURN_DEGREES - MIN_TURN_DEGREES)
        return if (random.nextBoolean()) from + turn else from - turn
    }

    companion object {
        const val FULL_TURN_DEGREES = 360f
        const val MIN_TURN_DEGREES = 60f
        const val MAX_TURN_DEGREES = 135f
        private const val CHANNEL_MAX = 255
        private const val OPAQUE_ALPHA = 0xFF shl 24
        private const val RED_SHIFT = 16
        private const val GREEN_SHIFT = 8

        /** Her renk, kendi payının bu oranında saf kalır; geri kalanında bir sonraki renge geçer. */
        const val COLOR_HOLD_SHARE = 0.6f

        /** Gökkuşağının sırasıyla renkleri (RGB): kırmızı, turuncu, sarı, yeşil, turkuaz, mavi, mor, pembe. */
        val PALETTE = intArrayOf(
            0xFF0000, 0xFF8C00, 0xFFE000, 0x00CC00,
            0x00C8E6, 0x0064FF, 0x8C00FF, 0xFF00A0,
        )

        /**
         * [hue] derecedeki renk (ARGB). Tam tur, [PALETTE] renkleri arasında eşit paylaştırılır; böylece
         * sarı ve turuncu gibi dar görünen tonlar da kırmızı, yeşil ve mavi kadar yer kaplar. Komşu iki
         * renk, payının [COLOR_HOLD_SHARE] kadarında saf kalır, sonra bir sonraki renge yumuşakça geçer.
         */
        fun colorOf(hue: Float): Int {
            val position = hue.mod(FULL_TURN_DEGREES) / FULL_TURN_DEGREES * PALETTE.size
            val index = position.toInt() % PALETTE.size
            val from = PALETTE[index]
            val to = PALETTE[(index + 1) % PALETTE.size]
            val t = blendAt(position - position.toInt())
            return OPAQUE_ALPHA or
                (mix(from, to, RED_SHIFT, t) shl RED_SHIFT) or
                (mix(from, to, GREEN_SHIFT, t) shl GREEN_SHIFT) or
                mix(from, to, 0, t)
        }

        /** Rengin payı içindeki [share] konumunda, bir sonraki renge ne kadar geçildiği (0 ile 1 arası). */
        private fun blendAt(share: Float): Float =
            smoothStep(((share - COLOR_HOLD_SHARE) / (1f - COLOR_HOLD_SHARE)).coerceAtLeast(0f))

        /** Geçişin yavaş başlayıp yavaş bitmesini sağlar; duraklarda kırılma görünmez. */
        private fun smoothStep(t: Float): Float = t * t * (3f - 2f * t)

        private fun mix(from: Int, to: Int, shift: Int, t: Float): Int {
            val a = (from shr shift) and CHANNEL_MAX
            val b = (to shr shift) and CHANNEL_MAX
            return (a + (b - a) * t).roundToInt()
        }
    }
}
