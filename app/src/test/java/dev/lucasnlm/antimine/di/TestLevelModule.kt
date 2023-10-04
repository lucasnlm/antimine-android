package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.common.level.repository.StatsRepository
import dev.lucasnlm.antimine.common.level.utils.ClockManager
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManager
import dev.lucasnlm.antimine.mocks.DisabledHapticFeedbackManager
import dev.lucasnlm.antimine.mocks.FixedMinefieldRepository
import dev.lucasnlm.antimine.mocks.MemorySavesRepository
import dev.lucasnlm.antimine.mocks.MemoryStatsRepository
import org.koin.dsl.bind
import org.koin.dsl.module

val TestLevelModule =
    module {
        single {
            ClockManager(get())
        }

        single {
            MemorySavesRepository()
        } bind SavesRepository::class

        single {
            MemoryStatsRepository()
        } bind StatsRepository::class

        single {
            FixedMinefieldRepository()
        } bind MinefieldRepository::class

        single {
            DisabledHapticFeedbackManager()
        } bind HapticFeedbackManager::class
    }
