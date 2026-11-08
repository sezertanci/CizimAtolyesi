package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Rect
import dev.sezertanci.drawingworkshop.domain.geometry.PixelRect

fun PixelRect.toRect(): Rect = Rect(left, top, right, bottom)

fun Rect.toPixelRect(): PixelRect = PixelRect(left, top, right, bottom)
