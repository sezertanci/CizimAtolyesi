package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Rect
import dev.sezertanci.drawingworkshop.domain.fill.FillBounds

fun FillBounds.toRect(): Rect = Rect(left, top, right, bottom)
