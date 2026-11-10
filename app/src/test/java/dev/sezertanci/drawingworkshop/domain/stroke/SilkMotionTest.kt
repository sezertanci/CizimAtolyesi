package dev.sezertanci.drawingworkshop.domain.stroke

import dev.sezertanci.drawingworkshop.domain.model.SilkStyle
import dev.sezertanci.drawingworkshop.domain.model.SilkTempo
import dev.sezertanci.drawingworkshop.domain.model.SilkThreads
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SilkMotionTest {
    @Test
    fun advance_ShortFrames_CarryOverRemainingTime() {
        val motion = createMotion()
        var steps = 0

        repeat(3) { motion.advance(SilkMotion.STEP_SECONDS * 0.5f) { steps++ } }

        assertEquals(1, steps)
    }

    @Test
    fun advance_LongPause_ProcessesAtMostMaxFrame() {
        val motion = createMotion()
        var steps = 0

        motion.advance(seconds = 3f) { steps++ }

        val maxSteps = (SilkMotion.MAX_FRAME_SECONDS / SilkMotion.STEP_SECONDS).toInt()
        assertTrue(steps in maxSteps - 1..maxSteps)
    }

    @Test
    fun advance_FingerStill_ThreadsStayNearFingerAndKeepMoving() = allStyles.forEach { style ->
        val motion = createMotion(style)
        var travelled = 0f

        repeat(FRAMES) {
            motion.advance(FRAME) {
                for (i in 0 until motion.threadCount) {
                    travelled += hypot(motion.x(i) - motion.previousX(i), motion.y(i) - motion.previousY(i))
                    assertTrue(distanceFrom(motion, i, START, START) <= SPREAD * 2f)
                }
            }
        }

        assertTrue(travelled > SPREAD)
    }

    @Test
    fun advance_FingerMoved_ThreadsFollowWithDelay() = allStyles.forEach { style ->
        val motion = createMotion(style)
        motion.moveTo(FAR, FAR)

        motion.advance(FRAME) {}
        val afterOneFrame = (0 until motion.threadCount).minOf { distanceFrom(motion, it, FAR, FAR) }
        repeat(FRAMES) { motion.advance(FRAME) {} }

        assertTrue(afterOneFrame > SPREAD * 2f)
        (0 until motion.threadCount).forEach { assertTrue(distanceFrom(motion, it, FAR, FAR) <= SPREAD * 2f) }
    }

    @Test
    fun advance_SameSeed_ProducesSameMotion() {
        val first = createMotion()
        val second = createMotion()

        repeat(FRAMES) {
            first.advance(FRAME) {}
            second.advance(FRAME) {}
        }

        assertEquals(first.x(0), second.x(0), 0f)
        assertEquals(first.y(first.threadCount - 1), second.y(second.threadCount - 1), 0f)
    }

    @Test
    fun advance_ZigzagFingerStill_ThreadsSwingAcrossHeading() {
        val motion = createMotion(SilkStyle(movement = SilkMovement.ZIGZAG))
        motion.moveTo(START + NUDGE, START)
        var alongSwing = 0f
        var acrossSwing = 0f

        repeat(FRAMES) {
            motion.advance(FRAME) {
                alongSwing = maxOf(alongSwing, abs(motion.x(0) - START - NUDGE))
                acrossSwing = maxOf(acrossSwing, abs(motion.y(0) - START))
            }
        }

        assertTrue(acrossSwing > SPREAD / 2f)
        assertTrue(alongSwing < acrossSwing / 2f)
    }

    private fun createMotion(style: SilkStyle = SilkStyle()) =
        SilkMotion(style.threads.threadCount, SPREAD, style.tempo.tuning, style.movement, Random(SEED))
            .apply { start(START, START) }

    private fun distanceFrom(motion: SilkMotion, thread: Int, x: Float, y: Float) =
        hypot(motion.x(thread) - x, motion.y(thread) - y)

    private val allStyles = SilkMovement.entries.flatMap { movement ->
        SilkTempo.entries.flatMap { tempo -> SilkThreads.entries.map { SilkStyle(movement, tempo, it) } }
    }

    private companion object {
        const val NUDGE = 10f
        const val SPREAD = 50f
        const val START = 500f
        const val FAR = 900f
        const val SEED = 42
        const val FRAME = 1f / 60f
        const val FRAMES = 240
    }
}
