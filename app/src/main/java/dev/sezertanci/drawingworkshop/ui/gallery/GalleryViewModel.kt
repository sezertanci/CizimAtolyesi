package dev.sezertanci.drawingworkshop.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sezertanci.drawingworkshop.data.DrawingPage
import dev.sezertanci.drawingworkshop.data.DrawingRepository
import dev.sezertanci.drawingworkshop.data.coloring.ColoringPage
import dev.sezertanci.drawingworkshop.data.coloring.ColoringPageCatalog
import dev.sezertanci.drawingworkshop.domain.drawing.Drawing
import dev.sezertanci.drawingworkshop.domain.sound.SoundSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GalleryUiState(
    val drawings: List<Drawing> = emptyList(),
    val hasMoreDrawings: Boolean = false,
    val trashCount: Int = 0,
    val coloringPages: List<ColoringPage> = emptyList(),
    val soundEnabled: Boolean = true,
)

/**
 * Açılış ekranı: kullanıcının resimleri, boyama sayfaları ve ses düğmesi. Açılışta süresi dolan çöp
 * kutusu resimleri temizlenir.
 */
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: DrawingRepository,
    private val thumbnailLoader: ThumbnailLoader,
    private val pageCatalog: ColoringPageCatalog,
    private val soundSettings: SoundSettings,
) : ViewModel() {

    /** Şu ana kadar istenen resim sayısı; kullanıcı aşağı kaydırdıkça bir sayfa büyür. */
    private val drawingLimit = MutableStateFlow(GalleryPaging.PAGE_SIZE)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val drawingPage: StateFlow<DrawingPage> = drawingLimit
        .flatMapLatest(repository::observeDrawings)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), DrawingPage(emptyList(), hasMore = false))

    val uiState: StateFlow<GalleryUiState> =
        combine(drawingPage, repository.observeTrash(), soundSettings.enabled) { page, trash, sound ->
            GalleryUiState(page.drawings, page.hasMore, trash.size, pageCatalog.pages, sound)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), GalleryUiState())

    init {
        viewModelScope.launch { repository.purgeExpiredTrash() }
    }

    fun moveToTrash(id: String) {
        viewModelScope.launch { repository.moveToTrash(id) }
    }

    /** Liste sonuna yaklaşıldığında çağrılır; önceki sayfa gelmeden yeni sayfa istenmez. */
    fun loadMoreDrawings() = drawingLimit.update { GalleryPaging.nextLimit(it, drawingPage.value) }

    fun toggleSound() = soundSettings.setEnabled(!soundSettings.enabled.value)

    suspend fun loadThumbnail(drawing: Drawing) = thumbnailLoader.load(drawing)
}

/** Ekran kısa süre görünmez olduğunda (ör. döndürme) akışı hemen kesmemek için bekleme süresi. */
internal const val STOP_TIMEOUT_MILLIS = 5_000L
