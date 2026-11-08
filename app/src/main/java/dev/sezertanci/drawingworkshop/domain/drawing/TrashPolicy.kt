package dev.sezertanci.drawingworkshop.domain.drawing

import java.util.concurrent.TimeUnit

/** Çöp kutusu kuralı: atılan resim 24 saat geri getirilebilir, sonra kalıcı olarak silinir. */
object TrashPolicy {
    const val RETENTION_HOURS: Int = 24
    val RETENTION_MILLIS: Long = TimeUnit.HOURS.toMillis(RETENTION_HOURS.toLong())
    private val HOUR_MILLIS: Long = TimeUnit.HOURS.toMillis(1)

    /** Bu andan önce çöp kutusuna atılmış resimlerin süresi dolmuştur. */
    fun expiryCutoff(now: Long): Long = now - RETENTION_MILLIS

    /** Kalıcı silmeye kalan süre, yukarı yuvarlanmış saat olarak; süre dolduysa 0. */
    fun remainingHours(deletedAt: Long, now: Long): Int {
        val remaining = (deletedAt + RETENTION_MILLIS - now).coerceAtLeast(0)
        return ((remaining + HOUR_MILLIS - 1) / HOUR_MILLIS).toInt()
    }
}
