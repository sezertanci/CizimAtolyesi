package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val PanelPadding = 12.dp
private val ItemSpacing = 10.dp

/**
 * Sağ panelin genişliği. Renk ve çıkartma panelleri aynı genişlikte olur; böylece araç değişince
 * tuvalin boyutu değişmez.
 */
internal val RightPanelWidth = 80.dp

/** Kenar panellerinin ortak iskeleti: ekran boyunca uzanan, kaydırılabilir, ortalanmış düğme sütunu. */
@Composable
internal fun PanelColumn(width: Dp, modifier: Modifier = Modifier, content: LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = modifier.width(width).fillMaxHeight(),
        contentPadding = PaddingValues(vertical = PanelPadding),
        verticalArrangement = Arrangement.spacedBy(ItemSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}
