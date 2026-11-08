package dev.sezertanci.drawingworkshop.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.sezertanci.drawingworkshop.domain.drawing.Drawing

/** @property templateId Resim bir boyama sayfasından açıldıysa sayfanın kimliği; boş tuvalse `null`. */
@Entity(tableName = "drawings")
data class DrawingEntity(
    @PrimaryKey val id: String,
    val updatedAt: Long,
    val deletedAt: Long?,
    val backgroundColor: Int,
    val templateId: String?,
)

fun DrawingEntity.toDomain() = Drawing(id, updatedAt, deletedAt, backgroundColor)
