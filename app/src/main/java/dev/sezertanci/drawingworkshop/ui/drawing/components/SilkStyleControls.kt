package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.model.SilkStyle
import dev.sezertanci.drawingworkshop.domain.model.SilkTempo
import dev.sezertanci.drawingworkshop.domain.model.SilkThreads
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.domain.stroke.SilkMovement
import dev.sezertanci.drawingworkshop.ui.components.DrawableIcon
import dev.sezertanci.drawingworkshop.ui.theme.WorkshopColors

private val GroupPadding = 4.dp
private val TitleSize = 14.sp
private val DividerWidth = 1.dp

/** İpek fırçasının seçili stili ve stil değiştiğinde çağrılacak işlem. */
class SilkStyleControls(
    val style: SilkStyle,
    val onStyleSelected: (SilkStyle) -> Unit,
)

/**
 * İpek fırçası düğmesi: ilk dokunuş aracı seçer, araç zaten seçiliyken dokunmak stil kutusunu açar.
 * Kutuda hareket, tempo ve teller yan yana üç sütundur; her sütundan bir seçim yapılır ve seçimler
 * birleşir. Kutu, birkaç seçim art arda yapılabilsin diye dışına dokununca kapanır.
 */
@Composable
internal fun SilkToolButton(selected: Boolean, controls: SilkStyleControls, onToolSelected: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ToolButton(ToolType.SILK, selected) { if (selected) expanded = true else onToolSelected() }
        ToolMenu(expanded = expanded, onDismiss = { expanded = false }) {
            SilkStyleGroups(controls.style, controls.onStyleSelected)
        }
    }
}

@Composable
private fun SilkStyleGroups(style: SilkStyle, onStyleSelected: (SilkStyle) -> Unit) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        StyleGroup(R.string.silk_group_movement) {
            ChoiceColumn(SilkMovement.entries, style.movement, { it.labelRes }, { onStyleSelected(style.copy(movement = it)) }) {
                DrawableIcon(it.iconRes)
            }
        }
        GroupDivider()
        StyleGroup(R.string.silk_group_tempo) {
            ChoiceColumn(SilkTempo.entries, style.tempo, { it.labelRes }, { onStyleSelected(style.copy(tempo = it)) }) {
                DrawableIcon(it.iconRes)
            }
        }
        GroupDivider()
        StyleGroup(R.string.silk_group_threads) {
            ChoiceColumn(SilkThreads.entries, style.threads, { it.labelRes }, { onStyleSelected(style.copy(threads = it)) }) {
                DrawableIcon(it.iconRes)
            }
        }
    }
}

/** Başlığı olan bir seçim sütunu. */
@Composable
private fun StyleGroup(@StringRes titleRes: Int, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(GroupPadding)) {
        Text(
            text = stringResource(titleRes),
            color = WorkshopColors.MenuText,
            fontSize = TitleSize,
            fontWeight = FontWeight.Bold,
        )
        content()
    }
}

@Composable
private fun GroupDivider() {
    VerticalDivider(
        thickness = DividerWidth,
        color = WorkshopColors.MenuText,
        modifier = Modifier.fillMaxHeight().padding(vertical = GroupPadding),
    )
}

@get:StringRes
private val SilkMovement.labelRes: Int
    get() = when (this) {
        SilkMovement.SPIN -> R.string.silk_movement_spin
        SilkMovement.FLOW -> R.string.silk_movement_flow
        SilkMovement.ZIGZAG -> R.string.silk_movement_zigzag
        SilkMovement.SMOKE -> R.string.silk_movement_smoke
    }

@get:DrawableRes
private val SilkMovement.iconRes: Int
    get() = when (this) {
        SilkMovement.SPIN -> R.drawable.ic_silk_spin
        SilkMovement.FLOW -> R.drawable.ic_silk_flow
        SilkMovement.ZIGZAG -> R.drawable.ic_silk_zigzag
        SilkMovement.SMOKE -> R.drawable.ic_silk_smoke
    }

@get:StringRes
private val SilkTempo.labelRes: Int
    get() = when (this) {
        SilkTempo.CALM -> R.string.silk_tempo_calm
        SilkTempo.WAVY -> R.string.silk_tempo_wavy
        SilkTempo.STORM -> R.string.silk_tempo_storm
    }

@get:DrawableRes
private val SilkTempo.iconRes: Int
    get() = when (this) {
        SilkTempo.CALM -> R.drawable.ic_silk_calm
        SilkTempo.WAVY -> R.drawable.ic_silk_wavy
        SilkTempo.STORM -> R.drawable.ic_silk_storm
    }

@get:StringRes
private val SilkThreads.labelRes: Int
    get() = when (this) {
        SilkThreads.SCATTERED -> R.string.silk_threads_scattered
        SilkThreads.TULLE -> R.string.silk_threads_tulle
        SilkThreads.DENSE -> R.string.silk_threads_dense
    }

@get:DrawableRes
private val SilkThreads.iconRes: Int
    get() = when (this) {
        SilkThreads.SCATTERED -> R.drawable.ic_silk_scattered
        SilkThreads.TULLE -> R.drawable.ic_silk_tulle
        SilkThreads.DENSE -> R.drawable.ic_silk_dense
    }
