package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.control.viewmodel.ControlViewModel
import dev.lucasnlm.antimine.custom.viewmodel.CreateGameViewModel
import dev.lucasnlm.antimine.history.viewmodel.HistoryViewModel
import dev.lucasnlm.antimine.level.viewmodel.EndGameDialogViewModel
import dev.lucasnlm.antimine.playgames.viewmodel.PlayGamesViewModel
import dev.lucasnlm.antimine.stats.viewmodel.StatsViewModel
import dev.lucasnlm.antimine.text.viewmodel.TextViewModel
import dev.lucasnlm.antimine.theme.viewmodel.ThemeViewModel
import org.koin.dsl.module

val ViewModelModule = module {
    single { AboutViewModel(get()) }
    factory { ControlViewModel(get()) }
    factory { CreateGameViewModel(get()) }
    factory { HistoryViewModel(get(), get()) }
    factory { EndGameDialogViewModel(get()) }
    factory { PlayGamesViewModel(get(), get()) }
    factory { StatsViewModel(get(), get()) }
    factory { TextViewModel(get()) }
    factory { ThemeViewModel(get()) }
    single {
        GameViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
    }
}
