package dev.lucasnlm.antimine.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dev.lucasnlm.antimine.instant.InstantAppManager

@Module
class AppModule(
    private val application: Application
) {
    @Provides
    fun provideContext(): Context = application.applicationContext

    @Provides
    fun provideInstantAppManager(): InstantAppManager = InstantAppManager(application.applicationContext)
}
