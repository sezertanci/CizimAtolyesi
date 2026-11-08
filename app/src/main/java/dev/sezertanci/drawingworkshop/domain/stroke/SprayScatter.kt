package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** Sprey aracı için bir dairenin içine düzgün dağılmış rastgele noktalar üretir. */
class SprayScatter(private val random: Random = Random.Default) {

    /** [radius] yarıçaplı daireye [count] nokta yerleştirir ve [target] dizisine x,y çiftleri olarak ekler. */
    fun scatter(center: StrokePoint, radius: Float, count: Int, target: FloatList) {
        repeat(count) {
            // Karekök, noktaların merkeze yığılmadan dairenin tamamına eşit dağılmasını sağlar.
            val distance = radius * sqrt(random.nextFloat())
            val angle = random.nextFloat() * FULL_TURN
            target.add(center.x + distance * cos(angle))
            target.add(center.y + distance * sin(angle))
        }
    }

    private companion object {
        const val FULL_TURN = (2 * PI).toFloat()
    }
}
