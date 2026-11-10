package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.model.SilkStyle
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.ui.components.DrawableIcon

/** İpek fırçasının seçili stili ve stil değiştiğinde çağrılacak işlem. */
class SilkStyleControls(
    val style: SilkStyle,
    val onStyleSelected: (SilkStyle) -> Unit,
)

/** İpek fırçası düğmesi: ilk dokunuş aracı seçer, araç zaten seçiliyken dokunmak stil kutusunu açar. */
@Composable
internal fun SilkToolButton(selected: Boolean, controls: SilkStyleControls, onToolSelected: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ToolButton(ToolType.SILK, selected) { if (selected) expanded = true else onToolSelected() }
        ChoiceMenu(
            expanded = expanded,
            options = SilkStyle.entries,
            selected = controls.style,
            labelOf = { it.labelRes },
            onDismiss = { expanded = false },
            onSelected = controls.onStyleSelected,
        ) { DrawableIcon(it.iconRes) }
    }
}

@get:StringRes
private val SilkStyle.labelRes: Int
    get() = when (this) {
        SilkStyle.CALM -> R.string.silk_style_calm
        SilkStyle.WAVY -> R.string.silk_style_wavy
        SilkStyle.STORM -> R.string.silk_style_storm
        SilkStyle.TULLE -> R.string.silk_style_tulle
    }

@get:DrawableRes
private val SilkStyle.iconRes: Int
    get() = when (this) {
        SilkStyle.CALM -> R.drawable.ic_silk_calm
        SilkStyle.WAVY -> R.drawable.ic_silk_wavy
        SilkStyle.STORM -> R.drawable.ic_silk_storm
        SilkStyle.TULLE -> R.drawable.ic_silk_tulle
    }
