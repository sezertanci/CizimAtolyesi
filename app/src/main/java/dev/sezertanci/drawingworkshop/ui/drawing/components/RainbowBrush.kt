package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import dev.sezertanci.drawingworkshop.domain.stroke.RainbowHues

private const val RAINBOW_COLOR_STOPS = 12

/** Gökkuşağı renginin panellerdeki görünümü: renkleri bir daire boyunca dönen dolgu. */
internal val RainbowBrush: Brush = Brush.sweepGradient(
    (0..RAINBOW_COLOR_STOPS).map {
        Color(RainbowHues.colorOf(it * RainbowHues.FULL_TURN_DEGREES / RAINBOW_COLOR_STOPS))
    },
)
