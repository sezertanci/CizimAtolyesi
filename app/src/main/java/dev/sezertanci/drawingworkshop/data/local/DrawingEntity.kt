package dev.sezertanci.drawingworkshop.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.sezertanci.drawingworkshop.domain.drawing.Drawing

@Entity(tableName = "drawings")
data class DrawingEntity(
    @PrimaryKey val id: String,
    val updatedAt: Long,
    val deletedAt: Long?,
    val backgroundColor: Int,
)

fun DrawingEntity.toDomain() = Drawing(id, updatedAt, deletedAt, backgroundColor)
