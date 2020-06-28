package dev.lucasnlm.antimine.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.preferences.PreferencesManager
import dev.lucasnlm.antimine.core.preferences.PreferencesRepository
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class CommonModule {
    @Singleton
    @Provides
    fun providePreferencesRepository(
        preferencesManager: PreferencesManager
    ): IPreferencesRepository = PreferencesRepository(preferencesManager)

    @Singleton
    @Provides
    fun providePreferencesInteractor(
        @ApplicationContext context: Context
    ): PreferencesManager = PreferencesManager(context)

    @Singleton
    @Provides
    fun provideAnalyticsManager(): AnalyticsManager = DebugAnalyticsManager()
}
