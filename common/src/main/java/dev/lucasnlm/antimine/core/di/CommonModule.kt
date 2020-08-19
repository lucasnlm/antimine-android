package dev.lucasnlm.antimine.core.di

import android.view.ViewConfiguration
import dev.lucasnlm.antimine.common.level.repository.DimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.core.preferences.IPreferencesManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.preferences.PreferencesManager
import dev.lucasnlm.antimine.core.preferences.PreferencesRepository
import dev.lucasnlm.antimine.core.sound.ISoundManager
import dev.lucasnlm.antimine.core.sound.SoundManager
import dev.lucasnlm.antimine.core.themes.repository.IThemeRepository
import dev.lucasnlm.antimine.core.themes.repository.ThemeRepository
import org.koin.dsl.bind
import org.koin.dsl.module

val CommonModule = module {
    single { PreferencesManager(get()) } bind IPreferencesManager::class

    single { DimensionRepository(get(), get()) } bind IDimensionRepository::class

    single { PreferencesRepository(get(), ViewConfiguration.getLongPressTimeout()) } bind IPreferencesRepository::class

    single { SoundManager(get()) } bind ISoundManager::class

    single { ThemeRepository(get(), get()) } bind IThemeRepository::class
}
