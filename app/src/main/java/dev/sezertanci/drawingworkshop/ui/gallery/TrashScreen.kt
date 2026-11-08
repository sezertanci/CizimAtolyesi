package dev.sezertanci.drawingworkshop.ui.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.drawing.TrashPolicy
import dev.sezertanci.drawingworkshop.ui.components.EmojiIcon
import dev.sezertanci.drawingworkshop.ui.components.PanelButton
import dev.sezertanci.drawingworkshop.ui.components.screenContainer
import dev.sezertanci.drawingworkshop.ui.gallery.components.CardGrid
import dev.sezertanci.drawingworkshop.ui.gallery.components.CardLabel
import dev.sezertanci.drawingworkshop.ui.gallery.components.DrawingCard
import dev.sezertanci.drawingworkshop.ui.gallery.components.OverlayButtonSize
import dev.sezertanci.drawingworkshop.ui.gallery.components.ScreenHeader
import dev.sezertanci.drawingworkshop.ui.gallery.components.ThumbnailSource

/** Çöp kutusunun giriş noktası: ViewModel'i alır ve durumu ekrana bağlar. */
@Composable
fun TrashRoute(onBack: () -> Unit, viewModel: TrashViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    TrashScreen(items, viewModel::loadThumbnail, onBack, viewModel::restore)
}

@Composable
fun TrashScreen(
    items: List<TrashItem>?,
    thumbnails: ThumbnailSource,
    onBack: () -> Unit,
    onRestore: (String) -> Unit,
) {
    Column(Modifier.screenContainer()) {
        ScreenHeader(
            title = stringResource(R.string.trash_title),
            leading = {
                PanelButton(selected = false, description = stringResource(R.string.action_back), onClick = onBack) {
                    EmojiIcon(BACK_EMOJI)
                }
            },
        )
        when {
            items == null -> Unit
            items.isEmpty() -> EmptyTrash()
            else -> TrashGrid(items, thumbnails, onRestore)
        }
    }
}

@Composable
private fun TrashGrid(items: List<TrashItem>, thumbnails: ThumbnailSource, onRestore: (String) -> Unit) {
    CardGrid {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = stringResource(R.string.trash_hint, TrashPolicy.RETENTION_HOURS),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        items(items, key = { it.drawing.id }) { item ->
            DrawingCard(item.drawing, thumbnails, description = stringResource(R.string.trash_item)) {
                PanelButton(
                    selected = false,
                    description = stringResource(R.string.action_restore),
                    onClick = { onRestore(item.drawing.id) },
                    modifier = Modifier.align(Alignment.TopEnd),
                    size = OverlayButtonSize,
                ) { EmojiIcon(RESTORE_EMOJI) }
                CardLabel(
                    text = stringResource(R.string.trash_remaining_hours, item.remainingHours),
                    modifier = Modifier.align(Alignment.BottomStart),
                )
            }
        }
    }
}

@Composable
private fun EmptyTrash() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.trash_empty), style = MaterialTheme.typography.titleLarge)
    }
}

private const val BACK_EMOJI = "⬅️"
private const val RESTORE_EMOJI = "♻️"
