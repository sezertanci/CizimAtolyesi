package dev.sezertanci.drawingworkshop.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.data.coloring.ColoringPage
import dev.sezertanci.drawingworkshop.domain.drawing.Drawing
import dev.sezertanci.drawingworkshop.domain.model.Palette
import dev.sezertanci.drawingworkshop.ui.components.EmojiIcon
import dev.sezertanci.drawingworkshop.ui.components.PanelButton
import dev.sezertanci.drawingworkshop.ui.components.screenContainer
import dev.sezertanci.drawingworkshop.ui.gallery.components.CardGrid
import dev.sezertanci.drawingworkshop.ui.gallery.components.DrawingCard
import dev.sezertanci.drawingworkshop.ui.gallery.components.GalleryCard
import dev.sezertanci.drawingworkshop.ui.gallery.components.OverlayButtonSize
import dev.sezertanci.drawingworkshop.ui.gallery.components.ScreenHeader
import dev.sezertanci.drawingworkshop.ui.gallery.components.SectionTitle
import dev.sezertanci.drawingworkshop.ui.gallery.components.ThumbnailSource

/** Galerinin giriş noktası: ViewModel'i alır ve durumu ekrana bağlar. */
@Composable
fun GalleryRoute(
    onNewDrawing: () -> Unit,
    onOpenDrawing: (String) -> Unit,
    onOpenColoringPage: (String) -> Unit,
    onOpenTrash: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GalleryScreen(
        state = state,
        thumbnails = viewModel::loadThumbnail,
        onNewDrawing = onNewDrawing,
        onOpenDrawing = onOpenDrawing,
        onOpenColoringPage = onOpenColoringPage,
        onMoveToTrash = viewModel::moveToTrash,
        onOpenTrash = onOpenTrash,
        onToggleSound = viewModel::toggleSound,
    )
}

/**
 * Galeri aynı ızgarada iki bölümden oluşur: "Resimlerim" ve altında "Boyama sayfaları". Bir sayfaya
 * dokunmak o sayfadan yeni bir resim açar; sayfanın kendisi değişmez.
 */
@Composable
fun GalleryScreen(
    state: GalleryUiState,
    thumbnails: ThumbnailSource,
    onNewDrawing: () -> Unit,
    onOpenDrawing: (String) -> Unit,
    onOpenColoringPage: (String) -> Unit,
    onMoveToTrash: (String) -> Unit,
    onOpenTrash: () -> Unit,
    onToggleSound: () -> Unit,
) {
    Column(Modifier.screenContainer()) {
        ScreenHeader(
            title = stringResource(R.string.app_name),
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(HeaderButtonSpacing)) {
                    SoundButton(state.soundEnabled, onToggleSound)
                    TrashButton(state.trashCount, onOpenTrash)
                }
            },
        )
        CardGrid {
            item(span = { GridItemSpan(maxLineSpan) }) { SectionTitle(stringResource(R.string.gallery_my_drawings)) }
            item { NewDrawingCard(onNewDrawing) }
            items(state.drawings, key = Drawing::id) { drawing ->
                MyDrawingCard(drawing, thumbnails, onOpenDrawing, onMoveToTrash)
            }
            item(span = { GridItemSpan(maxLineSpan) }) { SectionTitle(stringResource(R.string.gallery_coloring_pages)) }
            items(state.coloringPages, key = ColoringPage::id) { page -> ColoringPageCard(page, onOpenColoringPage) }
        }
    }
}

@Composable
private fun NewDrawingCard(onClick: () -> Unit) {
    GalleryCard(description = stringResource(R.string.action_new_drawing), onClick = onClick) {
        Box(Modifier.align(Alignment.Center)) { EmojiIcon(NEW_DRAWING_EMOJI) }
    }
}

@Composable
private fun MyDrawingCard(
    drawing: Drawing,
    thumbnails: ThumbnailSource,
    onOpen: (String) -> Unit,
    onMoveToTrash: (String) -> Unit,
) {
    DrawingCard(
        drawing = drawing,
        thumbnails = thumbnails,
        description = stringResource(R.string.action_open_drawing),
        onClick = { onOpen(drawing.id) },
    ) {
        PanelButton(
            selected = false,
            description = stringResource(R.string.action_move_to_trash),
            onClick = { onMoveToTrash(drawing.id) },
            modifier = Modifier.align(Alignment.TopEnd),
            size = OverlayButtonSize,
        ) { EmojiIcon(TRASH_EMOJI) }
    }
}

@Composable
private fun ColoringPageCard(page: ColoringPage, onOpen: (String) -> Unit) {
    GalleryCard(
        description = stringResource(R.string.action_open_coloring_page),
        background = Color(Palette.WHITE),
        onClick = { onOpen(page.id) },
    ) {
        Image(
            painter = painterResource(page.art),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
}

/** Sesleri açıp kapatır; seçim uygulama kapansa da hatırlanır. */
@Composable
private fun SoundButton(enabled: Boolean, onClick: () -> Unit) {
    PanelButton(
        selected = false,
        description = stringResource(if (enabled) R.string.action_sound_off else R.string.action_sound_on),
        onClick = onClick,
    ) { EmojiIcon(if (enabled) SOUND_ON_EMOJI else SOUND_OFF_EMOJI) }
}

@Composable
private fun TrashButton(count: Int, onClick: () -> Unit) {
    BadgedBox(badge = { if (count > 0) Badge { Text(count.toString()) } }) {
        PanelButton(
            selected = false,
            description = stringResource(R.string.trash_title),
            onClick = onClick,
        ) { EmojiIcon(TRASH_EMOJI) }
    }
}

private const val NEW_DRAWING_EMOJI = "➕"
private const val TRASH_EMOJI = "🗑️"
private const val SOUND_ON_EMOJI = "🔊"
private const val SOUND_OFF_EMOJI = "🔇"
private val HeaderButtonSpacing = 16.dp
