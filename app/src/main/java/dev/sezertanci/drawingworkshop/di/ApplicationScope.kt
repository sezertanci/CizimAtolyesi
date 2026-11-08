package dev.sezertanci.drawingworkshop.di

import javax.inject.Qualifier

/**
 * Uygulama boyunca yaşayan coroutine kapsamı. Ekran kapansa bile yarıda kalmaması gereken işler
 * (ör. kayıt) burada çalışır.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
