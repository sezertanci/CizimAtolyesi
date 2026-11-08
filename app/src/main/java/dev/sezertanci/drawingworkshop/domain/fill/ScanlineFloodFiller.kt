package dev.sezertanci.drawingworkshop.domain.fill

import javax.inject.Inject
import kotlin.math.abs

/**
 * Satır taramalı (scanline) boya kovası.
 *
 * Özyineleme kullanmaz; büyük alanlarda bile yığın taşması olmaz. Başlangıç rengine [TOLERANCE]
 * kadar yakın pikseller doldurulur. Ardından kenardaki yarı saydam (kenar yumuşatmalı) pikseller
 * yeni rengin üzerine karıştırılır; böylece çizgi ile dolgu arasında beyaz hale kalmaz.
 */
class ScanlineFloodFiller @Inject constructor() : FloodFiller {

    override fun fill(
        pixels: IntArray,
        width: Int,
        height: Int,
        startX: Int,
        startY: Int,
        color: Int,
        walls: BooleanArray?,
    ): FillBounds? {
        if (startX !in 0 until width || startY !in 0 until height) return null
        val target = pixels[startY * width + startX]
        if (target == color) return null

        val region = FillRegion(pixels, width, height, target, walls)
        region.floodFrom(startX, startY, color)
        repeat(EDGE_BLEND_PASSES) { region.blendEdges(color) }
        return region.bounds()
    }

    private class FillRegion(
        private val pixels: IntArray,
        private val width: Int,
        private val height: Int,
        private val target: Int,
        private val walls: BooleanArray?,
    ) {
        private val filled = BooleanArray(pixels.size)
        private val stack = IntStack()
        private var minX = width
        private var minY = height
        private var maxX = -1
        private var maxY = -1

        fun floodFrom(x: Int, y: Int, color: Int) {
            stack.push(y * width + x)
            while (stack.isNotEmpty()) {
                val index = stack.pop()
                if (canFill(index)) fillSpan(index % width, index / width, color)
            }
        }

        private fun fillSpan(x: Int, y: Int, color: Int) {
            val row = y * width
            var left = x
            while (left > 0 && canFill(row + left - 1)) left--
            var right = x
            while (right < width - 1 && canFill(row + right + 1)) right++
            for (i in left..right) paint(row + i, color)
            include(left, y)
            include(right, y)
            if (y > 0) pushSpanSeeds(left, right, y - 1)
            if (y < height - 1) pushSpanSeeds(left, right, y + 1)
        }

        /** Komşu satırdaki her doldurulabilir aralığın ilk pikselini yığına ekler. */
        private fun pushSpanSeeds(left: Int, right: Int, y: Int) {
            val row = y * width
            var inSpan = false
            for (i in left..right) {
                val fillable = canFill(row + i)
                if (fillable && !inSpan) stack.push(row + i)
                inSpan = fillable
            }
        }

        fun blendEdges(color: Int) {
            if (maxX < 0) return
            val edges = IntStack()
            val x0 = (minX - 1).coerceAtLeast(0)
            val x1 = (maxX + 1).coerceAtMost(width - 1)
            val y0 = (minY - 1).coerceAtLeast(0)
            val y1 = (maxY + 1).coerceAtMost(height - 1)
            for (y in y0..y1) {
                for (x in x0..x1) {
                    val index = y * width + x
                    if (!filled[index] && isTranslucent(pixels[index]) && touchesFilled(x, y)) edges.push(index)
                }
            }
            while (edges.isNotEmpty()) {
                val index = edges.pop()
                pixels[index] = ColorMath.sourceOver(pixels[index], color)
                filled[index] = true
                include(index % width, index / width)
            }
        }

        fun bounds(): FillBounds? = if (maxX < 0) null else FillBounds(minX, minY, maxX + 1, maxY + 1)

        private fun touchesFilled(x: Int, y: Int): Boolean {
            val index = y * width + x
            return (x > 0 && filled[index - 1]) ||
                (x < width - 1 && filled[index + 1]) ||
                (y > 0 && filled[index - width]) ||
                (y < height - 1 && filled[index + width])
        }

        private fun canFill(index: Int): Boolean =
            !filled[index] && (walls == null || !walls[index]) && ColorMath.isClose(pixels[index], target, TOLERANCE)

        private fun paint(index: Int, color: Int) {
            pixels[index] = color
            filled[index] = true
        }

        private fun include(x: Int, y: Int) {
            if (x < minX) minX = x
            if (x > maxX) maxX = x
            if (y < minY) minY = y
            if (y > maxY) maxY = y
        }

        private fun isTranslucent(pixel: Int): Boolean = ColorMath.alpha(pixel) in 1 until ColorMath.MAX_CHANNEL
    }

    private class IntStack {
        private var items = IntArray(INITIAL_STACK_SIZE)
        private var size = 0

        fun push(value: Int) {
            if (size == items.size) items = items.copyOf(size * 2)
            items[size++] = value
        }

        fun pop(): Int = items[--size]

        fun isNotEmpty(): Boolean = size > 0
    }

    private companion object {
        const val TOLERANCE = 48
        const val EDGE_BLEND_PASSES = 2
        const val INITIAL_STACK_SIZE = 1024
    }
}

/** ARGB tam sayıları üzerinde renk hesapları. */
internal object ColorMath {
    const val MAX_CHANNEL = 255

    fun alpha(color: Int): Int = (color ushr 24) and MAX_CHANNEL

    private fun channel(color: Int, shift: Int): Int = (color shr shift) and MAX_CHANNEL

    fun isClose(a: Int, b: Int, tolerance: Int): Boolean =
        CHANNEL_SHIFTS.all { abs(channel(a, it) - channel(b, it)) <= tolerance }

    /** [source] rengini [destination] renginin üzerine koyar (premültiplike olmayan ARGB). */
    fun sourceOver(source: Int, destination: Int): Int {
        val sa = alpha(source)
        val da = alpha(destination)
        val outAlpha = sa + da * (MAX_CHANNEL - sa) / MAX_CHANNEL
        if (outAlpha == 0) return 0
        var result = outAlpha shl ALPHA_SHIFT
        for (shift in COLOR_SHIFTS) {
            val blended = (channel(source, shift) * sa +
                channel(destination, shift) * da * (MAX_CHANNEL - sa) / MAX_CHANNEL) / outAlpha
            result = result or (blended shl shift)
        }
        return result
    }

    private const val ALPHA_SHIFT = 24
    private val COLOR_SHIFTS = intArrayOf(16, 8, 0)
    private val CHANNEL_SHIFTS = intArrayOf(ALPHA_SHIFT, 16, 8, 0)
}
