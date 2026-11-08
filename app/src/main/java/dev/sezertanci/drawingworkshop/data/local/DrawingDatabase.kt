package dev.sezertanci.drawingworkshop.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DrawingEntity::class], version = 1)
abstract class DrawingDatabase : RoomDatabase() {
    abstract fun drawingDao(): DrawingDao

    companion object {
        const val NAME = "drawings.db"
    }
}
