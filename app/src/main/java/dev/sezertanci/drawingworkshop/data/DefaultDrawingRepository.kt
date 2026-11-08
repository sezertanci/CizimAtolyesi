package dev.sezertanci.drawingworkshop.data

import dev.sezertanci.drawingworkshop.data.file.DrawingFileStore
import dev.sezertanci.drawingworkshop.data.local.DrawingDao
import dev.sezertanci.drawingworkshop.data.local.DrawingEntity
import dev.sezertanci.drawingworkshop.data.local.toDomain
import dev.sezertanci.drawingworkshop.domain.drawing.Drawing
import dev.sezertanci.drawingworkshop.domain.drawing.TrashPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

class DefaultDrawingRepository @Inject constructor(
    private val dao: DrawingDao,
    private val fileStore: DrawingFileStore,
    private val clock: Clock,
) : DrawingRepository {

    override fun observeDrawings(): Flow<List<Drawing>> = dao.observeActive().map { it.map(DrawingEntity::toDomain) }

    override fun observeTrash(): Flow<List<Drawing>> = dao.observeTrashed().map { it.map(DrawingEntity::toDomain) }

    override suspend fun load(id: String): StoredDrawing? {
        val entity = dao.findById(id) ?: return null
        return StoredDrawing(entity.backgroundColor, fileStore.readLayer(id), entity.templateId)
    }

    /** Önce dosyalar yazılır, sonra kayıt güncellenir; dosya yazılamazsa kayıt değişmez. */
    override suspend fun save(request: SaveRequest) {
        try {
            if (!fileStore.write(request.id, request.layer, request.backgroundColor, request.templateId)) return
        } finally {
            request.layer.recycle()
        }
        val updated = dao.updateContent(request.id, request.updatedAt, request.backgroundColor)
        if (updated == 0) {
            dao.insert(
                DrawingEntity(
                    id = request.id,
                    updatedAt = request.updatedAt,
                    deletedAt = null,
                    backgroundColor = request.backgroundColor,
                    templateId = request.templateId,
                ),
            )
        }
    }

    override suspend fun moveToTrash(id: String) = dao.setDeletedAt(id, clock.millis())

    override suspend fun restore(id: String) = dao.setDeletedAt(id, null)

    /** Resim bu arada geri getirildiyse silinmez. */
    override suspend fun deleteForever(id: String) {
        if (dao.findById(id)?.deletedAt == null) return
        deleteWithFiles(listOf(id))
    }

    override suspend fun emptyTrash() = deleteWithFiles(dao.findTrashedBefore(Long.MAX_VALUE))

    override suspend fun purgeExpiredTrash() =
        deleteWithFiles(dao.findTrashedBefore(TrashPolicy.expiryCutoff(clock.millis())))

    /** Önce dosyalar silinir; arada uygulama kapanırsa kayıt kalır ve bir sonraki temizlikte yeniden denenir. */
    private suspend fun deleteWithFiles(ids: List<String>) {
        if (ids.isEmpty()) return
        ids.forEach { fileStore.delete(it) }
        dao.deleteAll(ids)
    }
}
