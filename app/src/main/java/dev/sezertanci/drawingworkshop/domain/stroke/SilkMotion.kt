package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * İpek fırçasının tellerini hareket ettirir.
 *
 * Tellerin parmağa göre nerede durmak istediğini seçili hareket biçimi ([SilkMovement]) belirler. Her tel
 * kendi hedefine bir yayla bağlıdır; bu yüzden parmağı gecikerek ve hafif sallanarak izler. Dizinin
 * uçlarındaki teller daha gevşek bağlıdır ve daha geride kalır, böylece şerit hareketin arkasında kıvrılır.
 *
 * Hareket, ekranın kare hızından bağımsız olsun diye sabit zaman adımlarıyla ilerler.
 */
class SilkMotion(
    val threadCount: Int,
    spread: Float,
    private val tuning: SilkTuning,
    movement: SilkMovement,
    random: Random = Random.Default,
) {
    private val x = FloatArray(threadCount)
    private val y = FloatArray(threadCount)
    private val previousX = FloatArray(threadCount)
    private val previousY = FloatArray(threadCount)
    private val velocityX = FloatArray(threadCount)
    private val velocityY = FloatArray(threadCount)
    private val offsets = FloatArray(threadCount * 2)
    private val pattern = SilkPattern.create(movement, threadCount, spread, tuning, random)
    private val stiffness = FloatArray(threadCount) { stiffnessOf(it, random) * pattern.grip }

    /** Yaylar sıkılaşınca sallanma oranı aynı kalsın diye sürtünme, sıkılığın kareköküyle artar. */
    private val damping = tuning.damping * sqrt(pattern.grip)
    private var targetX = 0f
    private var targetY = 0f
    private var headingX = 1f
    private var headingY = 0f
    private var pendingSeconds = 0f

    /** Çizginin başından beri geçen hareket süresi (saniye). */
    var time = 0f
        private set

    init {
        require(threadCount >= 2) { "İpek en az iki telden oluşur." }
    }

    /** Bütün teller parmağın değdiği noktadan başlar. */
    fun start(startX: Float, startY: Float) {
        x.fill(startX)
        y.fill(startY)
        previousX.fill(startX)
        previousY.fill(startY)
        targetX = startX
        targetY = startY
    }

    /** Parmağın yeni yeri; hareket yönü de yumuşakça bu yöne döner. */
    fun moveTo(fingerX: Float, fingerY: Float) {
        turnHeading(fingerX - targetX, fingerY - targetY)
        targetX = fingerX
        targetY = fingerY
    }

    /**
     * Geçen süre kadar sabit adım atar ve her adımdan sonra [onStep] çağrılır. Artan süre bir sonraki
     * kareye aktarılır. Uygulama takılırsa teller fırlamasın diye bir karede en çok [MAX_FRAME_SECONDS] işlenir.
     */
    fun advance(seconds: Float, onStep: () -> Unit) {
        pendingSeconds += min(seconds, MAX_FRAME_SECONDS)
        while (pendingSeconds >= STEP_SECONDS) {
            pendingSeconds -= STEP_SECONDS
            step()
            onStep()
        }
    }

    fun x(thread: Int): Float = x[thread]

    fun y(thread: Int): Float = y[thread]

    /** Telin bir önceki adımdaki konumu; tel izi bu noktadan şimdiki konuma çizilir. */
    fun previousX(thread: Int): Float = previousX[thread]

    fun previousY(thread: Int): Float = previousY[thread]

    private fun step() {
        time += STEP_SECONDS
        pattern.place(time, headingX, headingY, offsets)
        for (i in 0 until threadCount) pull(i, targetX + offsets[2 * i], targetY + offsets[2 * i + 1])
    }

    /** Teli yayla hedefe doğru çeker; sönümleme salınımın büyümesini önler. */
    private fun pull(i: Int, goalX: Float, goalY: Float) {
        previousX[i] = x[i]
        previousY[i] = y[i]
        velocityX[i] += (stiffness[i] * (goalX - x[i]) - damping * velocityX[i]) * STEP_SECONDS
        velocityY[i] += (stiffness[i] * (goalY - y[i]) - damping * velocityY[i]) * STEP_SECONDS
        x[i] += velocityX[i] * STEP_SECONDS
        y[i] += velocityY[i] * STEP_SECONDS
    }

    /** Parmağın titremesi yönü sarsmasın diye yön, her harekette yeni yöne ancak biraz yaklaşır. */
    private fun turnHeading(dx: Float, dy: Float) {
        val distance = hypot(dx, dy)
        if (distance == 0f) return
        val turnedX = headingX + (dx / distance - headingX) * HEADING_FOLLOW
        val turnedY = headingY + (dy / distance - headingY) * HEADING_FOLLOW
        val length = hypot(turnedX, turnedY)
        if (length == 0f) return
        headingX = turnedX / length
        headingY = turnedY / length
    }

    /** Uçlardaki teller daha gevşektir; her çizgide küçük rastgele farklar teli canlı gösterir. */
    private fun stiffnessOf(thread: Int, random: Random): Float {
        val looseness = 1f - EDGE_LOOSENESS * abs(threadPosition(thread, threadCount))
        val variation = 1f + STIFFNESS_VARIATION * random.nextSigned()
        return tuning.stiffness * looseness * variation
    }

    companion object {
        /** Bir hareket adımının süresi (saniye). */
        const val STEP_SECONDS = 1f / 120f

        /** Bir karede işlenen en uzun süre (saniye). */
        const val MAX_FRAME_SECONDS = 0.05f

        private const val EDGE_LOOSENESS = 0.55f
        private const val STIFFNESS_VARIATION = 0.15f

        /** Hareket yönünün her parmak hareketinde yeni yöne ne kadar yaklaştığı. */
        private const val HEADING_FOLLOW = 0.2f
    }
}
