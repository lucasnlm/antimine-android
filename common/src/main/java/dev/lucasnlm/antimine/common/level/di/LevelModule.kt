package dev.lucasnlm.antimine.common.level.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.database.AppDataBase
import dev.lucasnlm.antimine.common.level.repository.DimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.common.level.repository.StatsRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.HapticFeedbackInteractor
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackInteractor
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

@Module
open class LevelModule(
    private val application: Application
) {
    private val appDataBase by lazy {
        Room.databaseBuilder(application, AppDataBase::class.java, DATA_BASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    private val savesDao by lazy {
        appDataBase.saveDao()
    }

    private val statsDao by lazy {
        appDataBase.statsDao()
    }

    private val savesRepository by lazy {
        SavesRepository(savesDao)
    }

    private val statsRepository by lazy {
        StatsRepository(statsDao)
    }

    @Provides
    open fun provideGameEventObserver(): MutableLiveData<Event> = MutableLiveData()

    @Provides
    open fun provideClock(): Clock = Clock()

    @Provides
    open fun provideGameViewModelFactory(
        application: Application,
        eventObserver: MutableLiveData<Event>,
        savesRepository: ISavesRepository,
        statsRepository: IStatsRepository,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository,
        hapticFeedbackInteractor: IHapticFeedbackInteractor,
        minefieldRepository: IMinefieldRepository,
        analyticsManager: AnalyticsManager,
        clock: Clock
    ) = GameViewModelFactory(
        application,
        eventObserver,
        savesRepository,
        statsRepository,
        dimensionRepository,
        preferencesRepository,
        hapticFeedbackInteractor,
        minefieldRepository,
        analyticsManager,
        clock
    )

    @Provides
    open fun provideDimensionRepository(
        context: Context,
        preferencesRepository: IPreferencesRepository
    ): IDimensionRepository =
        DimensionRepository(context, preferencesRepository)

    @Provides
    open fun provideSavesRepository(): ISavesRepository = savesRepository

    @Provides
    open fun provideStatsRepository(): IStatsRepository = statsRepository

    @Provides
    open fun provideMinefieldRepository(): IMinefieldRepository = MinefieldRepository()

    @Provides
    open fun provideHapticFeedbackInteractor(
        application: Application,
        preferencesRepository: IPreferencesRepository
    ): IHapticFeedbackInteractor =
        HapticFeedbackInteractor(application, preferencesRepository)

    companion object {
        private const val DATA_BASE_NAME = "saves-db"
    }
}
