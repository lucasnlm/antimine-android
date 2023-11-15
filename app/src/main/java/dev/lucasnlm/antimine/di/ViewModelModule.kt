package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.control.viewmodel.ControlViewModel
import dev.lucasnlm.antimine.custom.viewmodel.CreateGameViewModel
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogViewModel
import dev.lucasnlm.antimine.history.viewmodel.HistoryViewModel
import dev.lucasnlm.antimine.l10n.viewmodel.LocalizationViewModel
import dev.lucasnlm.antimine.licenses.viewmodel.LicenseViewModel
import dev.lucasnlm.antimine.main.viewmodel.MainViewModel
import dev.lucasnlm.antimine.playgames.viewmodel.PlayGamesViewModel
import dev.lucasnlm.antimine.stats.viewmodel.StatsViewModel
import dev.lucasnlm.antimine.themes.viewmodel.ThemeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val ViewModelModule =
    module {
        viewModel { AboutViewModel(get(), get()) }
        viewModel { LicenseViewModel() }
        viewModel { ControlViewModel(get(), get()) }
        viewModel { CreateGameViewModel(get()) }
        viewModel { HistoryViewModel(get(), get(), get()) }
        viewModel { EndGameDialogViewModel(get(), get()) }
        viewModel { PlayGamesViewModel(get(), get()) }
        viewModel { StatsViewModel(get(), get(), get(), get()) }
        viewModel { ThemeViewModel(get(), get()) }
        viewModel { MainViewModel(get(), get(), get(), get(), get()) }
        viewModel { LocalizationViewModel(get(), get()) }
        viewModel {
            GameViewModel(
                get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
            )
        }
    }
