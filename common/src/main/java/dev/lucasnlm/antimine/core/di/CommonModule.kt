package dev.lucasnlm.antimine.core.di

import android.view.ViewConfiguration
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.core.sound.GameAudioManager
import dev.lucasnlm.antimine.core.sound.IGameAudioManager
import dev.lucasnlm.antimine.preferences.IPreferencesManager
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.PreferencesManager
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.antimine.ui.repository.ThemeRepository
import org.koin.dsl.bind
import org.koin.dsl.module

val CommonModule = module {
    single { PreferencesManager(get()) } bind IPreferencesManager::class

    single { DimensionRepository(get()) } bind IDimensionRepository::class

    single {
        PreferencesRepository(
            get(),
            ViewConfiguration.getLongPressTimeout(),
        )
    } bind IPreferencesRepository::class

    single { GameAudioManager(get(), get(), get()) } bind IGameAudioManager::class

    single {
        ThemeRepository(get(), get())
    } bind IThemeRepository::class
}
