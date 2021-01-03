package dev.lucasnlm.antimine.wear.di

import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val ViewModelModule = module {
    viewModel {
        GameViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
    }
}
