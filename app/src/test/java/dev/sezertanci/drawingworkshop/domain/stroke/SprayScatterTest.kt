package dev.sezertanci.drawingworkshop.domain.stroke

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot
import kotlin.random.Random

class SprayScatterTest {

    @Test
    fun scatter_ManyPoints_AllInsideRadius() {
        val scatter = SprayScatter(Random(SEED))
        val target = FloatList()
        val center = StrokePoint(100f, 50f)

        scatter.scatter(center, RADIUS, COUNT, target)

        assertEquals(COUNT * 2, target.size)
        for (i in 0 until target.size step 2) {
            val distance = hypot(target.values[i] - center.x, target.values[i + 1] - center.y)
            assertTrue("Nokta yarıçap dışında: $distance", distance <= RADIUS + EPSILON)
        }
    }

    private companion object {
        const val SEED = 42
        const val RADIUS = 12f
        const val COUNT = 500
        const val EPSILON = 0.001f
    }
}
