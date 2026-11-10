package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import dev.sezertanci.drawingworkshop.domain.model.StrokeSettings
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.domain.stroke.RainbowHues
import dev.sezertanci.drawingworkshop.domain.stroke.SilkMotion
import dev.sezertanci.drawingworkshop.domain.stroke.SprayScatter
import javax.inject.Inject

/** Seçili araca göre doğru boyayla yapılandırılmış bir [ActiveStroke] oluşturur. */
class StrokeFactory @Inject constructor(
    private val density: ScreenDensity,
    private val crayonTextures: CrayonTextureCache,
    private val sprayScatter: SprayScatter,
) {
    /**
     * Boya kovası bir çizgi değil tek dokunuşluk bir işlem olduğundan `null` döner. Tüm ölçüler
     * [zoom] oranına bölünür; görüntü büyütülmüşken çizgi ekranda yine aynı kalınlıkta görünür.
     */
    fun create(settings: StrokeSettings, backgroundColor: Int, zoom: Float): ActiveStroke? {
        if (settings.tool == ToolType.SILK) return null
        val style = TOOL_STYLES[settings.tool] ?: return null
        val scale = Scale(density, zoom)
        val width = scale.toPx(settings.size.widthDp) * style.widthFactor
        return when (settings.tool) {
            ToolType.SPRAY -> createSpray(settings.color, width, scale)
            ToolType.ERASER -> createEraser(backgroundColor, width, scale)
            ToolType.RAINBOW -> createRainbow(width, scale)
            else -> createPath(settings, width, style.alpha, scale)
        }
    }

    /**
     * Kendiliğinden ilerleyen çizgiyi (ipek fırçası) oluşturur; diğer araçlar için `null` döner.
     * Fırça boyutu şeridin genişliğini, seçili stil tellerin sayısını, hareketini ve saydamlığını belirler. Koyu zeminde renkler üst üste bindikçe toplanıp ışık
     * saçar; açık zeminde ise yarı saydam katmanlar halinde koyulaşır.
     */
    fun createLive(settings: StrokeSettings, backgroundColor: Int, zoom: Float): LiveStroke? {
        if (settings.tool != ToolType.SILK) return null
        val scale = Scale(density, zoom)
        val spread = scale.toPx(settings.size.widthDp) * TOOL_STYLES.getValue(ToolType.SILK).widthFactor
        val glows = Color.luminance(backgroundColor) < DARK_BACKGROUND_LUMINANCE
        val style = settings.silkStyle
        val threadPaint = silkPaint(settings.color, scale.toPx(style.threadWidthDp), style.threadAlpha, glows)
        val sheetPaint = silkPaint(settings.color, scale.toPx(SILK_SHEET_DP), style.sheetAlpha, glows)
        val motion = SilkMotion(style.threadCount, spread, style.tuning)
        return SilkStroke(motion, threadPaint, sheetPaint, settings.color)
    }

    private fun silkPaint(color: Int, width: Float, alpha: Int, glows: Boolean) =
        strokePaint(color, width, alpha).apply {
            if (glows) xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
        }

    /** Renk panelindeki seçim kullanılmaz; renk çizgi boyunca kendiliğinden ve rastgele değişir. */
    private fun createRainbow(width: Float, scale: Scale): ActiveStroke {
        val hues = RainbowHues(scale.toPx(RAINBOW_LEG_DP))
        val paint = strokePaint(hues.colorAt(0f), width, OPAQUE)
        return RainbowStroke(paint, scale.toPx(MIN_POINT_DISTANCE_DP), scale.toPx(RAINBOW_SECTION_DP), hues)
    }

    private fun createPath(settings: StrokeSettings, width: Float, alpha: Int, scale: Scale): ActiveStroke {
        val paint = strokePaint(settings.color, width, alpha)
        if (settings.tool == ToolType.CRAYON) paint.shader = crayonTextures.shaderFor(settings.color)
        return PathStroke(paint, paint, scale.toPx(MIN_POINT_DISTANCE_DP))
    }

    /** Önizlemede arka plan rengiyle boyar, boya katmanında ise pikselleri tamamen siler. */
    private fun createEraser(backgroundColor: Int, width: Float, scale: Scale): ActiveStroke {
        val preview = strokePaint(backgroundColor, width, OPAQUE)
        val clear = strokePaint(backgroundColor, width, OPAQUE).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        return PathStroke(preview, clear, scale.toPx(MIN_POINT_DISTANCE_DP))
    }

    private fun createSpray(color: Int, radius: Float, scale: Scale): ActiveStroke {
        val dotPaint = strokePaint(color, scale.toPx(SPRAY_DOT_DP), OPAQUE)
        return SprayStroke(dotPaint, radius, SPRAY_DOTS_PER_TOUCH, sprayScatter)
    }

    private fun strokePaint(color: Int, width: Float, alpha: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.alpha = alpha
        style = Paint.Style.STROKE
        strokeWidth = width
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    /** dp ölçülerini, büyütme oranını hesaba katarak resmin pikseline çevirir. */
    private class Scale(private val density: ScreenDensity, private val zoom: Float) {
        fun toPx(dp: Float): Float = density.toPx(dp) / zoom
    }

    /** Aracın boyut katsayısı ve saydamlığı. */
    private data class ToolStyle(val widthFactor: Float, val alpha: Int = OPAQUE)

    private companion object {
        const val OPAQUE = 255
        const val MIN_POINT_DISTANCE_DP = 1f
        const val SPRAY_DOT_DP = 1.5f
        const val SPRAY_DOTS_PER_TOUCH = 14

        /** Gökkuşağı fırçasının bir hedef renkten sonrakine geçerken aldığı yol. */
        const val RAINBOW_LEG_DP = 400f

        /** Gökkuşağı çizgisinin tek renk geçişiyle boyanan bölümlerinin uzunluğu. */
        const val RAINBOW_SECTION_DP = 6f

        /** Bu parlaklığın altındaki zeminler koyu sayılır; ipek orada ışık saçarak çizilir. */
        const val DARK_BACKGROUND_LUMINANCE = 0.4f
        const val SILK_SHEET_DP = 0.75f

        val TOOL_STYLES = mapOf(
            ToolType.PENCIL to ToolStyle(widthFactor = 0.35f, alpha = 230),
            ToolType.BRUSH to ToolStyle(widthFactor = 1f),
            ToolType.RAINBOW to ToolStyle(widthFactor = 1f),
            ToolType.SILK to ToolStyle(widthFactor = 6f),
            ToolType.MARKER to ToolStyle(widthFactor = 1.3f, alpha = 170),
            ToolType.CRAYON to ToolStyle(widthFactor = 1.2f),
            ToolType.SPRAY to ToolStyle(widthFactor = 1.6f),
            ToolType.ERASER to ToolStyle(widthFactor = 1.6f),
        )
    }
}
