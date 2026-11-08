package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.sticker.StickerCatalog
import dev.sezertanci.drawingworkshop.ui.components.EmojiIcon
import dev.sezertanci.drawingworkshop.ui.components.PanelButton

/** Çıkartma aracı seçiliyken renk panelinin yerinde görünen, kaydırılabilir çıkartma listesi. */
@Composable
fun StickerPanel(selectedSticker: String, onStickerSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.sticker_item)
    PanelColumn(width = RightPanelWidth, modifier = modifier) {
        items(StickerCatalog.stickers) { sticker ->
            PanelButton(
                selected = sticker == selectedSticker,
                description = description,
                onClick = { onStickerSelected(sticker) },
            ) { EmojiIcon(sticker) }
        }
    }
}
