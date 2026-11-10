package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * İpek fırçasının tellerini hareket ettirir.
 *
 * Teller parmağın çevresinde, dönen ve boyu nefes alır gibi uzayıp kısalan bir dizi oluşturur. Her tel
 * kendi hedefine bir yayla bağlıdır; bu yüzden parmağı gecikerek ve hafif sallanarak izler. Dizinin
 * uçlarındaki teller daha gevşek bağlıdır ve daha geride kalır, böylece şerit hareketin arkasında kıvrılır.
 * Parmak dursa da dizi dönmeyi sürdürür; teller [spread] çevresinden fazla uzaklaşmadan çizmeye devam eder.
 *
 * Hareket, ekranın kare hızından bağımsız olsun diye sabit zaman adımlarıyla ilerler.
 */
class SilkMotion(val threadCount: Int, private val spread: Float, random: Random = Random.Default) {
    private val x = FloatArray(threadCount)
    private val y = FloatArray(threadCount)
    private val previousX = FloatArray(threadCount)
    private val previousY = FloatArray(threadCount)
    private val velocityX = FloatArray(threadCount)
    private val velocityY = FloatArray(threadCount)
    private val stiffness = FloatArray(threadCount) { stiffnessOf(it, random) }
    private val spinSpeed = SPIN_SPEED * if (random.nextBoolean()) 1f else -1f
    private val phase = random.nextFloat() * FULL_TURN
    private var angle = random.nextFloat() * FULL_TURN
    private var targetX = 0f
    private var targetY = 0f
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
        moveTo(startX, startY)
    }

    fun moveTo(fingerX: Float, fingerY: Float) {
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
        angle += spinSpeed * STEP_SECONDS * (1f + SPIN_SWAY * sin(time * SWAY_SPEED + phase))
        val length = spread * (LENGTH_BASE + LENGTH_SWING * sin(time * BREATH_SPEED + phase))
        val wobble = spread * WOBBLE_SHARE
        val alongX = cos(angle)
        val alongY = sin(angle)
        for (i in 0 until threadCount) {
            val along = length * positionOf(i)
            val across = wobble * sin(time * WOBBLE_SPEED + i * WOBBLE_STEP + phase)
            pull(i, targetX + along * alongX - across * alongY, targetY + along * alongY + across * alongX)
        }
    }

    /** Teli yayla hedefe doğru çeker; sönümleme salınımın büyümesini önler. */
    private fun pull(i: Int, goalX: Float, goalY: Float) {
        previousX[i] = x[i]
        previousY[i] = y[i]
        velocityX[i] += (stiffness[i] * (goalX - x[i]) - DAMPING * velocityX[i]) * STEP_SECONDS
        velocityY[i] += (stiffness[i] * (goalY - y[i]) - DAMPING * velocityY[i]) * STEP_SECONDS
        x[i] += velocityX[i] * STEP_SECONDS
        y[i] += velocityY[i] * STEP_SECONDS
    }

    /** Telin dizideki yeri: bir uçta -1, ortada 0, öbür uçta 1. */
    private fun positionOf(thread: Int): Float = 2f * thread / (threadCount - 1) - 1f

    /** Uçlardaki teller daha gevşektir; her çizgide küçük rastgele farklar teli canlı gösterir. */
    private fun stiffnessOf(thread: Int, random: Random): Float {
        val looseness = 1f - EDGE_LOOSENESS * abs(positionOf(thread))
        val variation = 1f + STIFFNESS_VARIATION * (random.nextFloat() * 2f - 1f)
        return BASE_STIFFNESS * looseness * variation
    }

    companion object {
        /** Bir hareket adımının süresi (saniye). */
        const val STEP_SECONDS = 1f / 120f

        /** Bir karede işlenen en uzun süre (saniye). */
        const val MAX_FRAME_SECONDS = 0.05f

        private val FULL_TURN = (2 * PI).toFloat()
        private const val BASE_STIFFNESS = 110f
        private const val DAMPING = 11f
        private const val EDGE_LOOSENESS = 0.55f
        private const val STIFFNESS_VARIATION = 0.15f

        /** Dizinin dönme hızı (radyan/saniye) ve bu hızın ne kadar dalgalandığı. */
        private const val SPIN_SPEED = 2.4f
        private const val SPIN_SWAY = 0.6f
        private const val SWAY_SPEED = 0.9f

        /** Dizinin yarı boyu, [spread] değerinin bu oranları arasında uzayıp kısalır. */
        private const val LENGTH_BASE = 0.65f
        private const val LENGTH_SWING = 0.35f
        private const val BREATH_SPEED = 1.7f

        /** Tellerin dizi çizgisinden yana sapması. */
        private const val WOBBLE_SHARE = 0.25f
        private const val WOBBLE_SPEED = 5f
        private const val WOBBLE_STEP = 0.9f
    }
}
