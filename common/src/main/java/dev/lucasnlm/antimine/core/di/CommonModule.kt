package dev.lucasnlm.antimine.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lucasnlm.antimine.common.BuildConfig
import dev.lucasnlm.antimine.common.level.repository.DimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.core.analytics.IAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.ProdAnalyticsManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.preferences.PreferencesManager
import dev.lucasnlm.antimine.core.preferences.PreferencesRepository
import dev.lucasnlm.antimine.core.sound.ISoundManager
import dev.lucasnlm.antimine.core.sound.SoundManager
import dev.lucasnlm.antimine.core.themes.repository.IThemeRepository
import dev.lucasnlm.antimine.core.themes.repository.ThemeRepository
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class CommonModule {
    @Provides
    fun provideDimensionRepository(
        @ApplicationContext context: Context,
        preferencesRepository: IPreferencesRepository
    ): IDimensionRepository =
        DimensionRepository(context, preferencesRepository)

    @Singleton
    @Provides
    fun providePreferencesRepository(
        preferencesManager: PreferencesManager
    ): IPreferencesRepository = PreferencesRepository(preferencesManager)

    @Provides
    fun providePreferencesInteractor(
        @ApplicationContext context: Context
    ): PreferencesManager = PreferencesManager(context)

    @Provides
    fun provideSoundManager(
        @ApplicationContext context: Context
    ): ISoundManager = SoundManager(context)

    @Provides
    fun provideThemeRepository(
        @ApplicationContext context: Context,
        preferencesRepository: IPreferencesRepository
    ): IThemeRepository = ThemeRepository(context, preferencesRepository)
}
