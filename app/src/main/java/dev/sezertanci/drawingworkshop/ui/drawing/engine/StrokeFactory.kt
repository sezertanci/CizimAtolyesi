package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import dev.sezertanci.drawingworkshop.domain.model.StrokeSettings
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.domain.stroke.SprayScatter
import javax.inject.Inject

/** Seçili araca göre doğru boyayla yapılandırılmış bir [ActiveStroke] oluşturur. */
class StrokeFactory @Inject constructor(
    private val density: ScreenDensity,
    private val crayonTextures: CrayonTextureCache,
    private val sprayScatter: SprayScatter,
) {
    /** Boya kovası bir çizgi değil tek dokunuşluk bir işlem olduğundan `null` döner. */
    fun create(settings: StrokeSettings, backgroundColor: Int): ActiveStroke? {
        val style = TOOL_STYLES[settings.tool] ?: return null
        val width = density.toPx(settings.size.widthDp) * style.widthFactor
        return when (settings.tool) {
            ToolType.SPRAY -> createSpray(settings.color, width)
            ToolType.ERASER -> createEraser(backgroundColor, width)
            else -> createPath(settings, width, style.alpha)
        }
    }

    private fun createPath(settings: StrokeSettings, width: Float, alpha: Int): ActiveStroke {
        val paint = strokePaint(settings.color, width, alpha)
        if (settings.tool == ToolType.CRAYON) paint.shader = crayonTextures.shaderFor(settings.color)
        return PathStroke(paint, paint, minPointDistance())
    }

    /** Önizlemede arka plan rengiyle boyar, boya katmanında ise pikselleri tamamen siler. */
    private fun createEraser(backgroundColor: Int, width: Float): ActiveStroke {
        val preview = strokePaint(backgroundColor, width, OPAQUE)
        val clear = strokePaint(backgroundColor, width, OPAQUE).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        return PathStroke(preview, clear, minPointDistance())
    }

    private fun createSpray(color: Int, radius: Float): ActiveStroke {
        val dotPaint = strokePaint(color, density.toPx(SPRAY_DOT_DP), OPAQUE)
        return SprayStroke(dotPaint, radius, SPRAY_DOTS_PER_TOUCH, sprayScatter)
    }

    private fun minPointDistance(): Float = density.toPx(MIN_POINT_DISTANCE_DP)

    private fun strokePaint(color: Int, width: Float, alpha: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.alpha = alpha
        style = Paint.Style.STROKE
        strokeWidth = width
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    /** Aracın boyut katsayısı ve saydamlığı. */
    private data class ToolStyle(val widthFactor: Float, val alpha: Int = OPAQUE)

    private companion object {
        const val OPAQUE = 255
        const val MIN_POINT_DISTANCE_DP = 1f
        const val SPRAY_DOT_DP = 1.5f
        const val SPRAY_DOTS_PER_TOUCH = 14

        val TOOL_STYLES = mapOf(
            ToolType.PENCIL to ToolStyle(widthFactor = 0.35f, alpha = 230),
            ToolType.BRUSH to ToolStyle(widthFactor = 1f),
            ToolType.MARKER to ToolStyle(widthFactor = 1.3f, alpha = 170),
            ToolType.CRAYON to ToolStyle(widthFactor = 1.2f),
            ToolType.SPRAY to ToolStyle(widthFactor = 1.6f),
            ToolType.ERASER to ToolStyle(widthFactor = 1.6f),
        )
    }
}
