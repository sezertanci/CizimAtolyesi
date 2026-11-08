package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.core.graphics.createBitmap
import javax.inject.Inject
import kotlin.random.Random

/**
 * Pastel boya için pürüzlü doku üretir: rengi rastgele saydamlıktaki piksellerden oluşan küçük bir
 * döşeme (tile). Aynı renk tekrar seçildiğinde doku yeniden üretilmez.
 */
class CrayonTextureCache @Inject constructor() {
    private var cachedColor: Int? = null
    private var cachedShader: BitmapShader? = null

    fun shaderFor(color: Int): BitmapShader {
        val cached = cachedShader
        if (cached != null && cachedColor == color) return cached
        return createShader(color).also {
            cachedColor = color
            cachedShader = it
        }
    }

    private fun createShader(color: Int): BitmapShader {
        val random = Random(SEED)
        val rgb = color and RGB_MASK
        val pixels = IntArray(TILE_SIZE * TILE_SIZE) {
            val alpha = if (random.nextFloat() < GAP_RATIO) 0 else random.nextInt(MIN_GRAIN_ALPHA, MAX_ALPHA + 1)
            (alpha shl ALPHA_SHIFT) or rgb
        }
        val tile = createBitmap(TILE_SIZE, TILE_SIZE)
        tile.setPixels(pixels, 0, TILE_SIZE, 0, 0, TILE_SIZE, TILE_SIZE)
        return BitmapShader(tile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    private companion object {
        const val TILE_SIZE = 48
        const val SEED = 7
        const val GAP_RATIO = 0.22f
        const val MIN_GRAIN_ALPHA = 120
        const val MAX_ALPHA = 255
        const val ALPHA_SHIFT = 24
        const val RGB_MASK = 0x00FFFFFF
    }
}
