package dev.sezertanci.drawingworkshop.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sezertanci.drawingworkshop.data.DrawingRepository
import dev.sezertanci.drawingworkshop.domain.drawing.Drawing
import dev.sezertanci.drawingworkshop.domain.drawing.TrashPolicy
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

/** Çöp kutusundaki resim ve kalıcı silinmesine kalan saat. */
data class TrashItem(val drawing: Drawing, val remainingHours: Int)

/** Çöp kutusu: resimler buradan geri getirilir veya hemen silinir; süresi dolanlar ekran açılırken temizlenir. */
@HiltViewModel
class TrashViewModel @Inject constructor(
    private val repository: DrawingRepository,
    private val thumbnailLoader: ThumbnailLoader,
    private val clock: Clock,
) : ViewModel() {

    /** Liste henüz okunmadıysa `null`; böylece "çöp kutusu boş" yazısı yanlışlıkla görünmez. */
    val items: StateFlow<List<TrashItem>?> = repository.observeTrash()
        .map { drawings -> drawings.map(::toItem) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    init {
        viewModelScope.launch { repository.purgeExpiredTrash() }
    }

    fun restore(id: String) {
        viewModelScope.launch { repository.restore(id) }
    }

    fun deleteForever(id: String) {
        viewModelScope.launch { repository.deleteForever(id) }
    }

    fun emptyTrash() {
        viewModelScope.launch { repository.emptyTrash() }
    }

    suspend fun loadThumbnail(drawing: Drawing) = thumbnailLoader.load(drawing)

    private fun toItem(drawing: Drawing): TrashItem {
        val now = clock.millis()
        return TrashItem(drawing, TrashPolicy.remainingHours(drawing.deletedAt ?: now, now))
    }
}
