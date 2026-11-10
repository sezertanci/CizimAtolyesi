package dev.sezertanci.drawingworkshop.ui.drawing

import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.Palette
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
    val strokeColor: Int = Palette.RED,
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
}
