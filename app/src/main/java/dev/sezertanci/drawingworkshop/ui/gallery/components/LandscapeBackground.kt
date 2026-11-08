package dev.sezertanci.drawingworkshop.ui.gallery.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import kotlin.random.Random

private val SkyTop = Color(0xFF4A90D9)
private val SkyBottom = Color(0xFFBFE3F5)

/**
 * Gökyüzü degradesi üzerine güneş, gökkuşağı, dağ, bulut, tepe ve ağaç resimlerini rastgele dizer.
 * Düzen, ekran her açıldığında değişir; döndürme gibi yeniden oluşturmalarda ise aynı kalır.
 */
@Composable
fun LandscapeBackground(modifier: Modifier = Modifier) {
    val seed = rememberSaveable { Random.nextInt() }
    val items = remember(seed) { randomLandscape(Random(seed)) }
    BoxWithConstraints(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyTop, SkyBottom)))) {
        items.forEach { item -> SceneImage(item, maxWidth, maxHeight) }
    }
}

@Composable
private fun SceneImage(item: SceneItem, screenWidth: Dp, screenHeight: Dp) {
    val painter = painterResource(item.image)
    val width = screenWidth * item.width
    val height = width * (painter.intrinsicSize.height / painter.intrinsicSize.width) * item.heightScale
    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .offset(x = screenWidth * item.left, y = screenHeight * item.bottom - height)
            .size(width, height),
    )
}
