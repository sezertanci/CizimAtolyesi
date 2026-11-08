package dev.sezertanci.drawingworkshop.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.sezertanci.drawingworkshop.data.DefaultDrawingRepository
import dev.sezertanci.drawingworkshop.data.DrawingRepository
import dev.sezertanci.drawingworkshop.data.export.DrawingExporter
import dev.sezertanci.drawingworkshop.data.export.MediaStoreDrawingExporter
import dev.sezertanci.drawingworkshop.data.file.DrawingFileStore
import dev.sezertanci.drawingworkshop.data.file.InternalDrawingFileStore
import dev.sezertanci.drawingworkshop.data.local.DrawingDao
import dev.sezertanci.drawingworkshop.data.local.DrawingDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindDrawingRepository(impl: DefaultDrawingRepository): DrawingRepository

    @Binds
    abstract fun bindDrawingFileStore(impl: InternalDrawingFileStore): DrawingFileStore

    @Binds
    abstract fun bindDrawingExporter(impl: MediaStoreDrawingExporter): DrawingExporter

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): DrawingDatabase =
            Room.databaseBuilder(context, DrawingDatabase::class.java, DrawingDatabase.NAME).build()

        @Provides
        fun provideDrawingDao(database: DrawingDatabase): DrawingDao = database.drawingDao()

        @Provides
        @IoDispatcher
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

        @Provides
        @Singleton
        @ApplicationScope
        fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        @Provides
        fun provideClock(): Clock = Clock.systemDefaultZone()
    }
}
