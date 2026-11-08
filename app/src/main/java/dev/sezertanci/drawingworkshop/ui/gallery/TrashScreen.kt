package dev.sezertanci.drawingworkshop.ui.gallery

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.drawing.TrashPolicy
import dev.sezertanci.drawingworkshop.ui.components.ConfirmDialog
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
    TrashScreen(
        items = items,
        thumbnails = viewModel::loadThumbnail,
        actions = TrashActions(
            onBack = onBack,
            onRestore = viewModel::restore,
            onDeleteForever = viewModel::deleteForever,
            onEmptyTrash = viewModel::emptyTrash,
        ),
    )
}

/** Çöp kutusu ekranındaki düğmelerin karşılıkları. */
class TrashActions(
    val onBack: () -> Unit,
    val onRestore: (String) -> Unit,
    val onDeleteForever: (String) -> Unit,
    val onEmptyTrash: () -> Unit,
)

@Composable
fun TrashScreen(items: List<TrashItem>?, thumbnails: ThumbnailSource, actions: TrashActions) {
    var confirmingEmpty by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.screenContainer()) {
        ScreenHeader(
            title = stringResource(R.string.trash_title),
            leading = {
                PanelButton(selected = false, description = stringResource(R.string.action_back), onClick = actions.onBack) {
                    EmojiIcon(BACK_EMOJI)
                }
            },
            trailing = {
                if (!items.isNullOrEmpty()) {
                    PanelButton(
                        selected = false,
                        description = stringResource(R.string.action_empty_trash),
                        onClick = { confirmingEmpty = true },
                    ) { EmojiIcon(EMPTY_TRASH_EMOJI) }
                }
            },
        )
        when {
            items == null -> Unit
            items.isEmpty() -> EmptyTrash()
            else -> TrashGrid(items, thumbnails, actions)
        }
    }
    if (confirmingEmpty) {
        ConfirmDialog(
            emoji = EMPTY_TRASH_EMOJI,
            title = stringResource(R.string.empty_trash_confirm_title),
            onConfirm = {
                confirmingEmpty = false
                actions.onEmptyTrash()
            },
            onDismiss = { confirmingEmpty = false },
        )
    }
}

@Composable
private fun TrashGrid(items: List<TrashItem>, thumbnails: ThumbnailSource, actions: TrashActions) {
    CardGrid {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = stringResource(R.string.trash_hint, TrashPolicy.RETENTION_HOURS),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        items(items, key = { it.drawing.id }) { item ->
            DrawingCard(item.drawing, thumbnails, description = stringResource(R.string.trash_item)) {
                CardActionButton(DELETE_EMOJI, R.string.action_delete_forever, Modifier.align(Alignment.TopStart)) {
                    actions.onDeleteForever(item.drawing.id)
                }
                CardActionButton(RESTORE_EMOJI, R.string.action_restore, Modifier.align(Alignment.TopEnd)) {
                    actions.onRestore(item.drawing.id)
                }
                CardLabel(
                    text = stringResource(R.string.trash_remaining_hours, item.remainingHours),
                    modifier = Modifier.align(Alignment.BottomStart),
                )
            }
        }
    }
}

/** Kartın köşesindeki küçük düğme. */
@Composable
private fun CardActionButton(emoji: String, @StringRes descriptionRes: Int, modifier: Modifier, onClick: () -> Unit) {
    PanelButton(
        selected = false,
        description = stringResource(descriptionRes),
        onClick = onClick,
        modifier = modifier,
        size = OverlayButtonSize,
    ) { EmojiIcon(emoji) }
}

@Composable
private fun EmptyTrash() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.trash_empty), style = MaterialTheme.typography.titleLarge)
    }
}

private const val BACK_EMOJI = "⬅️"
private const val RESTORE_EMOJI = "♻️"
private const val DELETE_EMOJI = "❌"
private const val EMPTY_TRASH_EMOJI = "🔥"
