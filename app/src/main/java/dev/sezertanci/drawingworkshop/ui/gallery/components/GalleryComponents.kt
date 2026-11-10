package dev.sezertanci.drawingworkshop.ui.gallery.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sezertanci.drawingworkshop.domain.drawing.Drawing
import dev.sezertanci.drawingworkshop.ui.components.clickableWithSound
import dev.sezertanci.drawingworkshop.ui.theme.WorkshopColors

/** Küçük resmi okuyan fonksiyon; ekranlar ViewModel'deki yükleyiciyi verir. */
typealias ThumbnailSource = suspend (Drawing) -> ImageBitmap?

private val CardMinWidth = 220.dp
internal val GridSpacing = 16.dp
private val CardShape = RoundedCornerShape(16.dp)
private val CardBorder = 2.dp
private val OverlayPadding = 6.dp
private val HeaderSpacing = 16.dp
private const val CARD_ASPECT_RATIO = 1.4f

/** Galeri ve çöp kutusunun ortak ızgarası: kartlar ekran genişliğine göre sütunlara dizilir. */
@Composable
fun CardGrid(
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(CardMinWidth),
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(GridSpacing),
        horizontalArrangement = Arrangement.spacedBy(GridSpacing),
        verticalArrangement = Arrangement.spacedBy(GridSpacing),
        content = content,
    )
}

/** Yatay kayan satırdaki kartların sabit genişliği; ızgaradaki kartlarla aynı boyda görünür. */
fun Modifier.rowCardWidth(): Modifier = width(CardMinWidth)

/** Ekranın üst satırı: solda ve sağda düğmeler, ortada başlık. */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit = {},
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = GridSpacing, vertical = GridSpacing / 2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeaderSpacing),
    ) {
        leading()
        Text(text = title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        trailing()
    }
}

/** Izgarada tüm satırı kaplayan bölüm başlığı (ör. "Resimlerim"). */
@Composable
fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

/** Kart çerçevesi: yuvarlak köşe, ince kenarlık; dokunulabiliyorsa düğme gibi davranır. */
@Composable
fun GalleryCard(
    description: String,
    modifier: Modifier = Modifier,
    background: Color = WorkshopColors.ItemBackground,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val clickModifier = onClick?.let { Modifier.clickableWithSound(onClick = it) } ?: Modifier
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(CARD_ASPECT_RATIO)
            .clip(CardShape)
            .background(background)
            .border(CardBorder, WorkshopColors.Outline, CardShape)
            .then(clickModifier)
            .semantics { contentDescription = description },
        content = content,
    )
}

/** Resmin küçük görüntüsünü gösteren kart; [overlay] köşelere düğme ve etiket koymak için. */
@Composable
fun DrawingCard(
    drawing: Drawing,
    thumbnails: ThumbnailSource,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val image by produceState<ImageBitmap?>(null, drawing.id, drawing.updatedAt) { value = thumbnails(drawing) }
    GalleryCard(description, modifier, Color(drawing.backgroundColor), onClick) {
        image?.let { Image(it, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit) }
        Box(Modifier.fillMaxSize().padding(OverlayPadding)) { overlay() }
    }
}

/** Kart üzerindeki küçük bilgi etiketi (ör. kalan süre). */
@Composable
fun CardLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
            .clip(CardShape)
            .background(WorkshopColors.ItemBackground)
            .padding(horizontal = OverlayPadding * 2, vertical = OverlayPadding),
    )
}

internal val OverlayButtonSize = 52.dp
