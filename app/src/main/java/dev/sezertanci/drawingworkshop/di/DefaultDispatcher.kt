package dev.sezertanci.drawingworkshop.di

import javax.inject.Qualifier

/** CPU yoğun işler (ör. boya kovası) için kullanılan arka plan dağıtıcısı. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher
