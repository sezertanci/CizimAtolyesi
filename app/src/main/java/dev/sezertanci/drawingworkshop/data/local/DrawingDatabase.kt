package dev.sezertanci.drawingworkshop.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Sürüm geçmişi (şemalar `app/schemas` klasöründe saklanır):
 * - 1: resim listesi
 * - 2: boyama sayfası kimliği (`templateId`) eklendi; eski resimler korunur
 */
@Database(
    entities = [DrawingEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class DrawingDatabase : RoomDatabase() {
    abstract fun drawingDao(): DrawingDao

    companion object {
        const val NAME = "drawings.db"
    }
}
