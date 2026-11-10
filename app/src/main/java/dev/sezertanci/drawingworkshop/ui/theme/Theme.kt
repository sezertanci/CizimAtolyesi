package dev.sezertanci.drawingworkshop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Panel düğmelerinin ve açılan menülerin renkleri. Panel zemini `R.color.panel_background` kaynağından okunur. */
object WorkshopColors {
    val ItemBackground = Color(0xFFFFFFFF)
    val Selection = Color(0xFFFF9800)
    val SelectionBackground = Color(0xFFFFE0B2)
    val Outline = Color(0xFFBDBDBD)

    /** Açılan menülerin koyu füme zemini; menünün panelden ayrı bir pencere olduğu kolayca anlaşılır. */
    val MenuBackground = Color(0xFF3C3F44)
}

private val colorScheme = lightColorScheme(primary = WorkshopColors.Selection)

@Composable
fun DrawingWorkshopTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colorScheme, content = content)
}
