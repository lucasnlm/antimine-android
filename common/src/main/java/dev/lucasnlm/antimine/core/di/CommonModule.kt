package dev.lucasnlm.antimine.core.di

import android.view.ViewConfiguration
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.audio.GameAudioManagerImpl
import dev.lucasnlm.antimine.preferences.PreferencesManager
import dev.lucasnlm.antimine.preferences.PreferencesManagerImpl
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepositoryImpl
import dev.lucasnlm.antimine.ui.repository.ThemeRepository
import dev.lucasnlm.antimine.ui.repository.ThemeRepositoryImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val CommonModule =
    module {
        single { PreferencesManagerImpl(get()) } bind PreferencesManager::class

        single {
            PreferencesRepositoryImpl(
                get(),
                ViewConfiguration.getLongPressTimeout(),
            )
        } bind PreferencesRepository::class

        single { GameAudioManagerImpl(get(), get()) } bind GameAudioManager::class

        single {
            ThemeRepositoryImpl(get(), get())
        } bind ThemeRepository::class
    }
