package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Teller duman gibi dar bir kurdele olur ve parmağın izinde düzensizce savrulur. Parmak durdukça kurdele
 * bir yöne uzanıp katlanarak geri döner; aynı yolu tekrarladığı için parlak bir yumak oluşur.
 */
internal class SmokePattern(
    private val threadCount: Int,
    private val spread: Float,
    tuning: SilkTuning,
    random: Random,
) : SilkPattern {
    private val speed = tuning.pace * SMOKE_SPEED
    private val sway = spread * SWAY_SHARE
    private val swayAlong = SmoothNoise(random)
    private val swayAcross = SmoothNoise(random)
    private val plumeDirection = SmoothNoise(random)
    private val foldDepth = SmoothNoise(random)
    private val ribbonWidth = SmoothNoise(random)
    private val ribbonTilt = SmoothNoise(random)

    /** Uçlardaki teller biraz geriden gelir; kurdele böylece kıvrılır. */
    private val lag = FloatArray(threadCount) {
        abs(threadPosition(it, threadCount)) * EDGE_LAG + random.nextFloat() * LAG_VARIATION
    }

    /** Parmak durdukça büyüyen, hareket edince sıfıra inen uzanma mesafesi. */
    private var reach = 0f
    private var lastTime = 0f

    override fun place(time: Float, stillSeconds: Float, headingX: Float, headingY: Float, offsets: FloatArray) {
        followReach(time, stillSeconds)
        val turns = time * speed
        val width = spread * RIBBON_SHARE * ribbonWidth.at(turns)
        val tilt = ribbonTilt.at(turns) * TILT_RANGE
        val plume = plumeDirection.at(turns * DRIFT_RATE) * PLUME_RANGE
        val fold = reach * FOLD_SHARE * foldDepth.at(turns)
        for (i in 0 until threadCount) {
            val delayed = turns - lag[i]
            val loop = turns * LOOP_RATE - lag[i]
            val out = reach * (1f - cos(loop)) / 2f
            val bend = fold * sin(2f * loop)
            val side = width * threadPosition(i, threadCount)
            val along = sway * ALONG_SHARE * swayAlong.at(delayed) +
                out * cos(plume) - bend * sin(plume) + side * sin(tilt)
            val across = sway * swayAcross.at(delayed) + out * sin(plume) + bend * cos(plume) + side * cos(tilt)
            offsets.putOffset(i, along, across, headingX, headingY)
        }
    }

    private fun followReach(time: Float, stillSeconds: Float) {
        val goal = spread * STILL_REACH * min(stillSeconds / GROW_SECONDS, 1f)
        reach += (goal - reach) * min((time - lastTime) * REACH_FOLLOW, 1f)
        lastTime = time
    }

    private companion object {
        /** Kurdelenin şekil değiştirme hızı, tempo hızına oranla. */
        const val SMOKE_SPEED = 3f

        /** Savrulma genişliği (yayılma genişliğine oranla) ve ileri geri savrulmanın bu genişliğe oranı. */
        const val SWAY_SHARE = 1.6f
        const val ALONG_SHARE = 0.5f

        /** Uzanıp geri dönme sıklığı ve uzanma yönünün değişme hızı, şekil değiştirme hızına oranla. */
        const val LOOP_RATE = 0.9f
        const val DRIFT_RATE = 0.05f

        /** Uzanma yönünün hareket yönünden en fazla sapması (radyan). */
        const val PLUME_RANGE = 3f

        /** En uzun uzanma (yayılma genişliğine oranla), buna ulaşma süresi (saniye) ve izleme hızı. */
        const val STILL_REACH = 1.2f
        const val GROW_SECONDS = 4f
        const val REACH_FOLLOW = 3f

        /** Uçtaki kıvrımın derinliği, uzanma mesafesine oranla. */
        const val FOLD_SHARE = 0.8f

        /** Kurdelenin en geniş hâli (yayılma genişliğine oranla) ve en fazla yatması (radyan). */
        const val RIBBON_SHARE = 1.4f
        const val TILT_RANGE = 1.2f

        /** Uçlardaki tellerin gecikmesi ve teller arasındaki rastgele fark (radyan). */
        const val EDGE_LAG = 0.6f
        const val LAG_VARIATION = 0.15f
    }
}
