package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.mocks.FixedDimensionRepository
import dev.lucasnlm.antimine.mocks.MockPreferencesRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.ui.model.AppSkin
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.repository.Skins
import dev.lucasnlm.antimine.ui.repository.ThemeRepository
import dev.lucasnlm.antimine.ui.repository.Themes.darkTheme
import dev.lucasnlm.antimine.ui.repository.Themes.lightTheme
import io.mockk.mockk
import org.koin.dsl.bind
import org.koin.dsl.module

val TestCommonModule =
    module {
        single { FixedDimensionRepository() } bind DimensionRepository::class

        single { MockPreferencesRepository() } bind PreferencesRepository::class

        single {
            mockk<GameAudioManager>()
        } bind GameAudioManager::class

        single {
            object : ThemeRepository {
                override fun getCustomTheme(): AppTheme? = null

                override fun getSkin(): AppSkin = Skins.getAllSkins().first()

                override fun getTheme(): AppTheme = lightTheme()

                override fun getAllThemes(): List<AppTheme> = listOf(lightTheme())

                override fun getAllDarkThemes(): List<AppTheme> = listOf(darkTheme())

                override fun getAllSkins(): List<AppSkin> = Skins.getAllSkins()

                override fun setTheme(themeId: Long) {}

                override fun setSkin(skinId: Long) {}

                override fun reset(): AppTheme = lightTheme()
            }
        } bind ThemeRepository::class
    }
