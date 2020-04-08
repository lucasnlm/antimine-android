package dev.lucasnlm.antimine.core.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.preferences.PreferencesInteractor
import dev.lucasnlm.antimine.core.preferences.PreferencesRepository
import javax.inject.Singleton

@Module
class CommonModule {
    @Singleton
    @Provides
    fun providePreferencesRepository(
        preferencesInteractor: PreferencesInteractor
    ): IPreferencesRepository = PreferencesRepository(preferencesInteractor)

    @Singleton
    @Provides
    fun providePreferencesInteractor(
        application: Application
    ): PreferencesInteractor = PreferencesInteractor(application)

    @Singleton
    @Provides
    fun provideAnalyticsManager(): AnalyticsManager = DebugAnalyticsManager()
}
