package dev.lucasnlm.antimine.common.level.di

import androidx.room.Room
import dev.lucasnlm.antimine.common.level.database.AppDataBase
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.common.level.repository.ITipRepository
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.common.level.repository.StatsRepository
import dev.lucasnlm.antimine.common.level.repository.TipRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.HapticFeedbackManager
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackManager
import org.koin.dsl.bind
import org.koin.dsl.module

val LevelModule = module {
    single {
        Room.databaseBuilder(get(), AppDataBase::class.java, AppDataBase.DATA_BASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        get(AppDataBase::class).saveDao()
    }

    single {
        get(AppDataBase::class).statsDao()
    }

    single {
        Clock()
    }

    single {
        SavesRepository(get())
    } bind ISavesRepository::class

    single {
        StatsRepository(get())
    } bind IStatsRepository::class

    single {
        MinefieldRepository()
    } bind IMinefieldRepository::class

    single {
        HapticFeedbackManager(get())
    } bind IHapticFeedbackManager::class

    single {
        TipRepository(get())
    } bind ITipRepository::class
}
