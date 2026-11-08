package dev.sezertanci.drawingworkshop.data

import android.graphics.Bitmap
import dev.sezertanci.drawingworkshop.data.file.DrawingFileStore
import dev.sezertanci.drawingworkshop.data.local.DrawingDao
import dev.sezertanci.drawingworkshop.data.local.DrawingEntity
import dev.sezertanci.drawingworkshop.domain.drawing.TrashPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class DefaultDrawingRepositoryTest {
    private val now = 10 * TrashPolicy.RETENTION_MILLIS
    private val dao = FakeDrawingDao()
    private val fileStore = FakeFileStore()
    private val repository = DefaultDrawingRepository(
        dao,
        fileStore,
        Clock.fixed(Instant.ofEpochMilli(now), ZoneOffset.UTC),
    )

    @Test
    fun moveToTrash_ActiveDrawing_SetsDeletedAtToNow() = runBlocking {
        dao.rows["a"] = entity("a", deletedAt = null)

        repository.moveToTrash("a")

        assertEquals(now, dao.rows.getValue("a").deletedAt)
    }

    @Test
    fun restore_TrashedDrawing_ClearsDeletedAt() = runBlocking {
        dao.rows["a"] = entity("a", deletedAt = now)

        repository.restore("a")

        assertNull(dao.rows.getValue("a").deletedAt)
    }

    @Test
    fun purgeExpiredTrash_MixedDrawings_DeletesOnlyExpiredWithFiles() = runBlocking {
        dao.rows["expired"] = entity("expired", deletedAt = now - TrashPolicy.RETENTION_MILLIS)
        dao.rows["recent"] = entity("recent", deletedAt = now - 1)
        dao.rows["active"] = entity("active", deletedAt = null)

        repository.purgeExpiredTrash()

        assertEquals(setOf("recent", "active"), dao.rows.keys)
        assertEquals(listOf("expired"), fileStore.deleted)
    }

    @Test
    fun load_DrawingFromColoringPage_ReturnsTemplateId() = runBlocking {
        dao.rows["a"] = entity("a", deletedAt = null).copy(templateId = "cat")

        val stored = repository.load("a")

        assertEquals("cat", stored?.templateId)
    }

    @Test
    fun load_UnknownId_ReturnsNull() = runBlocking {
        assertNull(repository.load("missing"))
    }

    private fun entity(id: String, deletedAt: Long?) = DrawingEntity(id, updatedAt = 0, deletedAt, backgroundColor = 0, templateId = null)
}

private class FakeDrawingDao : DrawingDao {
    val rows = linkedMapOf<String, DrawingEntity>()
    private val changes = MutableStateFlow(0)

    override fun observeActive(): Flow<List<DrawingEntity>> = changes.map { rows.values.filter { it.deletedAt == null } }

    override fun observeTrashed(): Flow<List<DrawingEntity>> = changes.map { rows.values.filter { it.deletedAt != null } }

    override suspend fun findById(id: String) = rows[id]

    override suspend fun insert(entity: DrawingEntity) {
        rows.putIfAbsent(entity.id, entity)
    }

    override suspend fun updateContent(id: String, updatedAt: Long, backgroundColor: Int): Int {
        val row = rows[id] ?: return 0
        rows[id] = row.copy(updatedAt = updatedAt, backgroundColor = backgroundColor)
        return 1
    }

    override suspend fun setDeletedAt(id: String, deletedAt: Long?) {
        rows[id]?.let { rows[id] = it.copy(deletedAt = deletedAt) }
    }

    override suspend fun findTrashedBefore(cutoff: Long) =
        rows.values.filter { it.deletedAt != null && it.deletedAt <= cutoff }.map { it.id }

    override suspend fun deleteAll(ids: List<String>) {
        ids.forEach(rows::remove)
    }
}

private class FakeFileStore : DrawingFileStore {
    val deleted = mutableListOf<String>()

    override suspend fun write(id: String, layer: Bitmap, backgroundColor: Int, templateId: String?) = true

    override suspend fun readLayer(id: String): Bitmap? = null

    override suspend fun readThumbnail(id: String): Bitmap? = null

    override suspend fun delete(id: String) {
        deleted += id
    }
}
