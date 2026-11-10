package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.sezertanci.drawingworkshop.ui.components.PanelButton
import dev.sezertanci.drawingworkshop.ui.theme.WorkshopColors

private val MenuItemPadding = 6.dp

/** Panel düğmesinin yanında açılan koyu füme kutu. */
@Composable
internal fun ToolMenu(expanded: Boolean, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = WorkshopColors.MenuBackground,
        content = content,
    )
}

/** Seçenekleri alt alta simgeli düğmeler olarak gösterir; seçili olan turuncu çerçevelidir. */
@Composable
internal fun <T> ChoiceColumn(
    options: List<T>,
    selected: T,
    labelOf: (T) -> Int,
    onSelected: (T) -> Unit,
    icon: @Composable (T) -> Unit,
) {
    Column {
        options.forEach { option ->
            PanelButton(
                selected = option == selected,
                description = stringResource(labelOf(option)),
                onClick = { onSelected(option) },
                modifier = Modifier.padding(MenuItemPadding),
            ) { icon(option) }
        }
    }
}

/** Tek bir seçim için açılan kutu; seçim yapılınca kapanır. */
@Composable
internal fun <T> ChoiceMenu(
    expanded: Boolean,
    options: List<T>,
    selected: T,
    labelOf: (T) -> Int,
    onDismiss: () -> Unit,
    onSelected: (T) -> Unit,
    icon: @Composable (T) -> Unit,
) {
    ToolMenu(expanded, onDismiss) {
        ChoiceColumn(
            options = options,
            selected = selected,
            labelOf = labelOf,
            onSelected = {
                onDismiss()
                onSelected(it)
            },
            icon = icon,
        )
    }
}
