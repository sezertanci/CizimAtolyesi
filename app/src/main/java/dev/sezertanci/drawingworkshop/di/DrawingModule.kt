package dev.sezertanci.drawingworkshop.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.sezertanci.drawingworkshop.domain.fill.FloodFiller
import dev.sezertanci.drawingworkshop.domain.fill.ScanlineFloodFiller
import dev.sezertanci.drawingworkshop.domain.stroke.SprayScatter
import dev.sezertanci.drawingworkshop.ui.drawing.engine.ScreenDensity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
abstract class DrawingModule {

    @Binds
    abstract fun bindFloodFiller(impl: ScanlineFloodFiller): FloodFiller

    companion object {
        @Provides
        @DefaultDispatcher
        fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

        @Provides
        fun provideScreenDensity(@ApplicationContext context: Context): ScreenDensity =
            ScreenDensity(context.resources.displayMetrics.density)

        @Provides
        fun provideSprayScatter(): SprayScatter = SprayScatter()
    }
}
