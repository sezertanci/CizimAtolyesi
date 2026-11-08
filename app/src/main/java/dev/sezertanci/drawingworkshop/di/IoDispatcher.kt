package dev.sezertanci.drawingworkshop.di

import javax.inject.Qualifier

/** Dosya ve veritabanı işleri için kullanılan arka plan dağıtıcısı. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
