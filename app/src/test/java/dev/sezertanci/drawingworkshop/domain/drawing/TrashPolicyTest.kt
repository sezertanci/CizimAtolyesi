package dev.sezertanci.drawingworkshop.domain.drawing

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class TrashPolicyTest {
    private val hour = TimeUnit.HOURS.toMillis(1)
    private val deletedAt = 1_000_000L

    @Test
    fun remainingHours_JustDeleted_ReturnsFullRetention() {
        assertEquals(TrashPolicy.RETENTION_HOURS, TrashPolicy.remainingHours(deletedAt, deletedAt))
    }

    @Test
    fun remainingHours_PartialHourLeft_RoundsUp() {
        val now = deletedAt + TrashPolicy.RETENTION_MILLIS - hour / 2

        assertEquals(1, TrashPolicy.remainingHours(deletedAt, now))
    }

    @Test
    fun remainingHours_Expired_ReturnsZero() {
        val now = deletedAt + TrashPolicy.RETENTION_MILLIS + hour

        assertEquals(0, TrashPolicy.remainingHours(deletedAt, now))
    }

    @Test
    fun expiryCutoff_Now_IsRetentionBefore() {
        assertEquals(deletedAt, TrashPolicy.expiryCutoff(deletedAt + TrashPolicy.RETENTION_MILLIS))
    }
}
