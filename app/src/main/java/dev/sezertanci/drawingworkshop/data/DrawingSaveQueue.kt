package dev.sezertanci.drawingworkshop.data

import android.database.SQLException
import android.util.Log
import dev.sezertanci.drawingworkshop.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kayıtları sırayla, uygulama kapsamında yazar. Çizim ekranı kapansa bile son kayıt tamamlanır ve
 * aynı resmin iki kaydı birbirini ezmez.
 */
@Singleton
class DrawingSaveQueue @Inject constructor(
    private val repository: DrawingRepository,
    @ApplicationScope scope: CoroutineScope,
) {
    private val requests = Channel<SaveRequest>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (request in requests) {
                try {
                    repository.save(request)
                } catch (e: SQLException) {
                    Log.e(TAG, "Resim kaydedilemedi", e)
                }
            }
        }
    }

    fun enqueue(request: SaveRequest) {
        requests.trySend(request)
    }

    private companion object {
        const val TAG = "DrawingSaveQueue"
    }
}
