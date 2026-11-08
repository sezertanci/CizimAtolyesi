package dev.sezertanci.drawingworkshop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import dev.sezertanci.drawingworkshop.R

/** Tüm ekranların ortak zemini: tam ekran, panel rengi ve kamera çentiğinden uzak. */
@Composable
fun Modifier.screenContainer(): Modifier = this
    .fillMaxSize()
    .background(colorResource(R.color.panel_background))
    .windowInsetsPadding(WindowInsets.displayCutout)
