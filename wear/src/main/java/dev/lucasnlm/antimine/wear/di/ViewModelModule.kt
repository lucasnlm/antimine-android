package dev.lucasnlm.antimine.wear.di

import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.control.viewmodel.ControlViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val ViewModelModule =
    module {
        viewModel { ControlViewModel(get(), get()) }
        viewModel {
            GameViewModel(
                get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
            )
        }
    }
