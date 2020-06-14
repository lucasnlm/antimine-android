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
import dev.lucasnlm.antimine.core.preferences.PreferencesInteractor
import dev.lucasnlm.antimine.core.preferences.PreferencesRepository

@Module
@InstallIn(ApplicationComponent::class)
class CommonApplicationModule {
    @Provides
    fun providePreferencesRepository(
        preferencesInteractor: PreferencesInteractor
    ): IPreferencesRepository = PreferencesRepository(preferencesInteractor)

    @Provides
    fun providePreferencesInteractor(
        @ApplicationContext context: Context
    ): PreferencesInteractor = PreferencesInteractor(context)

    @Provides
    fun provideAnalyticsManager(): AnalyticsManager = DebugAnalyticsManager()
}
