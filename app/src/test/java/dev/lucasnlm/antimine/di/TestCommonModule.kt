package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.core.audio.IGameAudioManager
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.mocks.FixedDimensionRepository
import dev.lucasnlm.antimine.mocks.MockPreferencesRepository
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.ui.model.AppSkin
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.antimine.ui.repository.Skins
import dev.lucasnlm.antimine.ui.repository.Themes.lightTheme
import io.mockk.mockk
import org.koin.dsl.bind
import org.koin.dsl.module

val TestCommonModule = module {
    single { FixedDimensionRepository() } bind IDimensionRepository::class

    single { MockPreferencesRepository() } bind IPreferencesRepository::class

    single {
        mockk<IGameAudioManager>()
    } bind IGameAudioManager::class

    single {
        object : IThemeRepository {
            override fun getCustomTheme(): AppTheme? = null

            override fun getSkin(): AppSkin = Skins.getAllSkins().first()

            override fun getTheme(): AppTheme = lightTheme()

            override fun getAllThemes(): List<AppTheme> = listOf(lightTheme())

            override fun getAllSkins(): List<AppSkin> = Skins.getAllSkins()

            override fun setTheme(themeId: Long) {}

            override fun setSkin(skinId: Long) {}

            override fun reset(): AppTheme = lightTheme()
        }
    } bind IThemeRepository::class
}
