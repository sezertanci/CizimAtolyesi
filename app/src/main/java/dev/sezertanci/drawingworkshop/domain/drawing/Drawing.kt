package dev.sezertanci.drawingworkshop.domain.drawing

/**
 * Kullanıcının kaydedilmiş bir resmi. Resimlerin adı yoktur; galeride küçük resimleriyle görünür.
 *
 * @property deletedAt Çöp kutusuna atıldığı an; resim galerideyse `null`.
 */
data class Drawing(
    val id: String,
    val updatedAt: Long,
    val deletedAt: Long?,
    val backgroundColor: Int,
)
