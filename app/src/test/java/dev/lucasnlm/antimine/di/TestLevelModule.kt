package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.common.level.repository.MemorySavesRepository
import dev.lucasnlm.antimine.common.level.repository.MemoryStatsRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackManager
import dev.lucasnlm.antimine.mocks.DisabledHapticFeedbackManager
import dev.lucasnlm.antimine.mocks.FixedMinefieldRepository
import org.koin.dsl.bind
import org.koin.dsl.module

val TestLevelModule = module {
    single {
        Clock()
    }

    single {
        MemorySavesRepository()
    } bind ISavesRepository::class

    single {
        MemoryStatsRepository()
    } bind IStatsRepository::class

    single {
        FixedMinefieldRepository()
    } bind IMinefieldRepository::class

    single {
        DisabledHapticFeedbackManager()
    } bind IHapticFeedbackManager::class
}
