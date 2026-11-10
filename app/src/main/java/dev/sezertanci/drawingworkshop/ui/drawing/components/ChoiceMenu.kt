package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.sezertanci.drawingworkshop.ui.components.PanelButton
import dev.sezertanci.drawingworkshop.ui.theme.WorkshopColors

private val MenuItemPadding = 6.dp

/** Panel düğmesinin yanında açılan, seçenekleri simgeli düğmeler olarak gösteren seçim kutusu. */
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
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss, containerColor = WorkshopColors.MenuBackground) {
        options.forEach { option ->
            PanelButton(
                selected = option == selected,
                description = stringResource(labelOf(option)),
                onClick = {
                    onDismiss()
                    onSelected(option)
                },
                modifier = Modifier.padding(MenuItemPadding),
            ) { icon(option) }
        }
    }
}
