package dev.sezertanci.drawingworkshop.data

import android.graphics.Bitmap
import dev.sezertanci.drawingworkshop.data.file.DrawingFileStore
import dev.sezertanci.drawingworkshop.data.local.DrawingDao
import dev.sezertanci.drawingworkshop.data.local.DrawingEntity
import dev.sezertanci.drawingworkshop.domain.drawing.TrashPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun deleteForever_TrashedDrawing_DeletesRowAndFiles() = runBlocking {
        dao.rows["trashed"] = entity("trashed", deletedAt = now)
        dao.rows["other"] = entity("other", deletedAt = now)

        repository.deleteForever("trashed")

        assertEquals(setOf("other"), dao.rows.keys)
        assertEquals(listOf("trashed"), fileStore.deleted)
    }

    @Test
    fun deleteForever_RestoredDrawing_KeepsDrawing() = runBlocking {
        dao.rows["a"] = entity("a", deletedAt = null)

        repository.deleteForever("a")

        assertEquals(setOf("a"), dao.rows.keys)
        assertEquals(emptyList<String>(), fileStore.deleted)
    }

    @Test
    fun emptyTrash_MixedDrawings_DeletesAllTrashedWithFiles() = runBlocking {
        dao.rows["recent"] = entity("recent", deletedAt = now)
        dao.rows["old"] = entity("old", deletedAt = now - TrashPolicy.RETENTION_MILLIS)
        dao.rows["active"] = entity("active", deletedAt = null)

        repository.emptyTrash()

        assertEquals(setOf("active"), dao.rows.keys)
        assertEquals(setOf("recent", "old"), fileStore.deleted.toSet())
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

    @Test
    fun observeDrawings_MoreThanLimit_ReturnsFirstPageNewestFirstWithHasMore() = runBlocking {
        (1..5).forEach { dao.rows["d$it"] = entity("d$it", deletedAt = null).copy(updatedAt = it.toLong()) }

        val page = repository.observeDrawings(limit = 3).first()

        assertEquals(listOf("d5", "d4", "d3"), page.drawings.map { it.id })
        assertTrue(page.hasMore)
    }

    @Test
    fun observeDrawings_ExactlyLimit_ReturnsAllWithoutMore() = runBlocking {
        (1..3).forEach { dao.rows["d$it"] = entity("d$it", deletedAt = null) }

        val page = repository.observeDrawings(limit = 3).first()

        assertEquals(3, page.drawings.size)
        assertFalse(page.hasMore)
    }

    @Test
    fun observeDrawings_TrashedDrawings_AreNotCountedAsMore() = runBlocking {
        dao.rows["active"] = entity("active", deletedAt = null)
        dao.rows["trashed"] = entity("trashed", deletedAt = now)

        val page = repository.observeDrawings(limit = 1).first()

        assertEquals(listOf("active"), page.drawings.map { it.id })
        assertFalse(page.hasMore)
    }

    private fun entity(id: String, deletedAt: Long?) = DrawingEntity(id, updatedAt = 0, deletedAt, backgroundColor = 0, templateId = null)
}

private class FakeDrawingDao : DrawingDao {
    val rows = linkedMapOf<String, DrawingEntity>()
    private val changes = MutableStateFlow(0)

    override fun observeActive(limit: Int): Flow<List<DrawingEntity>> = changes.map {
        rows.values.filter { it.deletedAt == null }.sortedByDescending { it.updatedAt }.take(limit)
    }

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
