package dev.sezertanci.drawingworkshop.ui.drawing

import dagger.hilt.android.scopes.ViewModelScoped
import dev.sezertanci.drawingworkshop.data.DrawingSaveQueue
import dev.sezertanci.drawingworkshop.data.SaveRequest
import dev.sezertanci.drawingworkshop.ui.drawing.engine.DrawingEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * Resmi kendiliğinden kaydeder: son değişiklikten 2 saniye sonra ve [saveNow] çağrıldığında
 * (ekrandan çıkarken, uygulama arka plana geçerken). Değişiklik yoksa kayıt yapılmaz.
 *
 * Ana iş parçacığında yalnızca boya katmanının kopyası alınır; PNG'ye çevirme ve yazma kayıt
 * kuyruğunda, arka planda yapılır.
 */
@ViewModelScoped
class AutoSaver @Inject constructor(
    private val engine: DrawingEngine,
    private val saveQueue: DrawingSaveQueue,
    private val clock: Clock,
) {
    private var drawingId: String? = null
    private var savedVersion = 0

    /** Yükleme bittikten sonra çağrılır; o anki hal kayıtlı kabul edilir. */
    fun start(id: String, scope: CoroutineScope) {
        drawingId = id
        savedVersion = engine.contentVersion.value
        scope.launch {
            engine.contentVersion.collectLatest { version ->
                if (version == savedVersion) return@collectLatest
                delay(AUTO_SAVE_DELAY)
                saveNow()
            }
        }
    }

    fun saveNow() {
        val id = drawingId ?: return
        val version = engine.contentVersion.value
        if (version == savedVersion) return
        val layer = engine.copyLayer() ?: return
        savedVersion = version
        saveQueue.enqueue(SaveRequest(id, layer, engine.backgroundColor, clock.millis()))
    }

    private companion object {
        val AUTO_SAVE_DELAY = 2.seconds
    }
}
