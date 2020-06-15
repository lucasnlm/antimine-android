package dev.lucasnlm.antimine.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lucasnlm.antimine.common.level.database.AppDataBase
import dev.lucasnlm.antimine.common.level.database.dao.SaveDao
import dev.lucasnlm.antimine.common.level.database.dao.StatsDao
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.common.level.repository.StatsRepository
import dev.lucasnlm.antimine.instant.InstantAppManageable
import dev.lucasnlm.antimine.instant.InstantAppManager

@Module
@InstallIn(ApplicationComponent::class)
class AppModule() {
    @Provides
    fun provideInstantAppManager(
        @ApplicationContext context: Context
    ): InstantAppManageable = InstantAppManager(context.applicationContext)

    @Provides
    fun provideAppDataBase(
        @ApplicationContext context: Context
    ): AppDataBase {
        return Room.databaseBuilder(context, AppDataBase::class.java, AppDataBase.NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideSaveDao(dataBase: AppDataBase): SaveDao = dataBase.saveDao()

    @Provides
    fun provideStatsDao(dataBase: AppDataBase): StatsDao = dataBase.statsDao()

    @Provides
    fun provideSavesRepository(savesDao: SaveDao): ISavesRepository = SavesRepository(savesDao)

    @Provides
    fun provideStatsRepository(statsDao: StatsDao): IStatsRepository = StatsRepository(statsDao)
}
