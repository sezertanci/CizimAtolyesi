package dev.sezertanci.drawingworkshop.ui.drawing

import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.Palette
import dev.sezertanci.drawingworkshop.domain.model.SilkStyle
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.domain.sticker.StickerCatalog
import dev.sezertanci.drawingworkshop.domain.symmetry.SymmetryMode

/** Renk panelinde seçilen rengin nereye uygulanacağı. */
enum class ColorTarget {
    STROKE,
    BACKGROUND,
}

data class DrawingUiState(
    val tool: ToolType = ToolType.BRUSH,
    val brushSize: BrushSize = BrushSize.DEFAULT,
    val silkStyle: SilkStyle = SilkStyle(),
    val strokeColor: Int = Palette.RED,

    /** Kalem için gökkuşağı seçildi mi; desteklemeyen araçlar bu sırada [strokeColor] kullanır. */
    val rainbow: Boolean = false,
    val backgroundColor: Int = Palette.WHITE,
    val colorTarget: ColorTarget = ColorTarget.STROKE,
    val sticker: String = StickerCatalog.default,
    val symmetry: SymmetryMode = SymmetryMode.OFF,

    /** Simetri açıkken boya kovası her kopya noktasını da doldurur. */
    val symmetricFill: Boolean = true,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isLoaded: Boolean = false,
    val exportStatus: ExportStatus = ExportStatus.IDLE,
) {
    /** Renk panelinde işaretli görünecek renk. */
    val selectedColor: Int get() = if (colorTarget == ColorTarget.STROKE) strokeColor else backgroundColor

    /** Seçili araç şu an gökkuşağıyla çiziyor mu. */
    val drawsRainbow: Boolean get() = rainbow && tool.supportsRainbow

    /** Gökkuşağı düğmesi yalnızca kalem rengi seçilirken ve araç destekliyorsa kullanılabilir. */
    val isRainbowAvailable: Boolean get() = colorTarget == ColorTarget.STROKE && tool.supportsRainbow

    /** Gökkuşağı düğmesi işaretli mi; işaretliyse düz renklerden hiçbiri işaretli görünmez. */
    val isRainbowSelected: Boolean get() = isRainbowAvailable && rainbow
}
