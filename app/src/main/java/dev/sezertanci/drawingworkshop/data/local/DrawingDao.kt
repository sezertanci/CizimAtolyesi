package dev.sezertanci.drawingworkshop.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawingDao {
    @Query("SELECT * FROM drawings WHERE deletedAt IS NULL ORDER BY updatedAt DESC")
    fun observeActive(): Flow<List<DrawingEntity>>

    @Query("SELECT * FROM drawings WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun observeTrashed(): Flow<List<DrawingEntity>>

    @Query("SELECT * FROM drawings WHERE id = :id")
    suspend fun findById(id: String): DrawingEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: DrawingEntity)

    /** Çöp kutusu bilgisine dokunmadan içeriği günceller; güncellenen satır sayısını döndürür. */
    @Query("UPDATE drawings SET updatedAt = :updatedAt, backgroundColor = :backgroundColor WHERE id = :id")
    suspend fun updateContent(id: String, updatedAt: Long, backgroundColor: Int): Int

    @Query("UPDATE drawings SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun setDeletedAt(id: String, deletedAt: Long?)

    @Query("SELECT id FROM drawings WHERE deletedAt IS NOT NULL AND deletedAt <= :cutoff")
    suspend fun findTrashedBefore(cutoff: Long): List<String>

    @Query("DELETE FROM drawings WHERE id IN (:ids)")
    suspend fun deleteAll(ids: List<String>)
}
