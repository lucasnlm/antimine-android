package dev.lucasnlm.antimine.common.level.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dev.lucasnlm.antimine.common.level.data.GameEvent
import dev.lucasnlm.antimine.common.level.database.AppDataBase
import dev.lucasnlm.antimine.common.level.database.dao.SaveDao
import dev.lucasnlm.antimine.common.level.repository.DimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.HapticFeedbackInteractor
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackInteractor
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

@Module
class LevelModule {
    @Provides
    fun provideGameEventObserver(): MutableLiveData<GameEvent> = MutableLiveData()

    @Provides
    fun provideClock(): Clock = Clock()

    @Provides
    fun provideGameViewModelFactory(
        application: Application,
        gameEventObserver: MutableLiveData<GameEvent>,
        savesRepository: ISavesRepository,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository,
        hapticFeedbackInteractor: IHapticFeedbackInteractor,
        analyticsManager: AnalyticsManager,
        clock: Clock
    ) = GameViewModelFactory(
        application, gameEventObserver, savesRepository,
        dimensionRepository, preferencesRepository, hapticFeedbackInteractor, analyticsManager, clock
    )

    @Provides
    fun provideDimensionRepository(
        context: Context,
        preferencesRepository: IPreferencesRepository
    ): IDimensionRepository =
        DimensionRepository(context, preferencesRepository)

    @Provides
    fun provideDataBase(application: Application): AppDataBase =
        Room.databaseBuilder(application, AppDataBase::class.java, DATA_BASE_NAME).build()

    @Provides
    fun provideSaveDao(appDataBase: AppDataBase): SaveDao = appDataBase.userDao()

    @Provides
    fun provideSavesRepository(saveDao: SaveDao): ISavesRepository = SavesRepository(saveDao)

    @Provides
    fun provideHapticFeedbackInteractor(
        application: Application,
        preferencesRepository: IPreferencesRepository
    ): IHapticFeedbackInteractor =
        HapticFeedbackInteractor(application, preferencesRepository)

    companion object {
        const val DATA_BASE_NAME = "saves-db"
    }
}
