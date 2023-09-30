package dev.lucasnlm.antimine.common.level.di

import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepositoryImpl
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.common.level.repository.SavesRepositoryImpl
import dev.lucasnlm.antimine.common.level.repository.StatsRepository
import dev.lucasnlm.antimine.common.level.repository.StatsRepositoryImpl
import dev.lucasnlm.antimine.common.level.repository.TipRepository
import dev.lucasnlm.antimine.common.level.repository.TipRepositoryImpl
import dev.lucasnlm.antimine.common.level.utils.Clock
import org.koin.dsl.bind
import org.koin.dsl.module

val LevelModule =
    module {
        single {
            Clock()
        }

        single {
            SavesRepositoryImpl(get(), get())
        } bind SavesRepository::class

        single {
            StatsRepositoryImpl(get())
        } bind StatsRepository::class

        single {
            MinefieldRepositoryImpl()
        } bind MinefieldRepository::class

        single {
            TipRepositoryImpl(get(), get())
        } bind TipRepository::class
    }
