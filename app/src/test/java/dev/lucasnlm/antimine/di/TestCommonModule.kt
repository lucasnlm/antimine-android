package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.sound.ISoundManager
import dev.lucasnlm.antimine.core.themes.model.AppTheme
import dev.lucasnlm.antimine.core.themes.repository.IThemeRepository
import dev.lucasnlm.antimine.core.themes.repository.Themes.LightTheme
import dev.lucasnlm.antimine.mocks.FixedDimensionRepository
import dev.lucasnlm.antimine.mocks.MockPreferencesRepository
import org.koin.dsl.bind
import org.koin.dsl.module

val TestCommonModule = module {
    single { FixedDimensionRepository() } bind IDimensionRepository::class

    single { MockPreferencesRepository() } bind IPreferencesRepository::class

    single {
        object : ISoundManager {
            override fun play(soundId: Int) { }
        }
    } bind ISoundManager::class

    single {
        object : IThemeRepository {
            override fun getCustomTheme(): AppTheme? = null

            override fun getTheme(): AppTheme = LightTheme

            override fun getAllThemes(): List<AppTheme> = listOf(LightTheme)

            override fun setTheme(themeId: Long) { }

            override fun reset(): AppTheme = LightTheme
        }
    } bind IThemeRepository::class
}
