package dev.lucasnlm.antimine.common.level.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import dagger.Module
import dagger.Provides
import dev.lucasnlm.antimine.common.level.mocks.FixedDimensionRepository
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.common.level.repository.MemorySavesRepository
import dev.lucasnlm.antimine.common.level.repository.MemoryStatsRepository
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.DisabledIHapticFeedbackInteractor
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackInteractor
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

@Module
class TestLevelModule(
    private val application: Application
) {
    @Provides
    fun provideGameEventObserver(): MutableLiveData<Event> = MutableLiveData()

    @Provides
    fun provideClock(): Clock = Clock()

    @Provides
    fun provideGameViewModelFactory(
        application: Application,
        eventObserver: MutableLiveData<Event>,
        savesRepository: ISavesRepository,
        statsRepository: IStatsRepository,
        dimensionRepository: IDimensionRepository,
        preferencesRepository: IPreferencesRepository,
        hapticFeedbackInteractor: IHapticFeedbackInteractor,
        minefieldRepository: MinefieldRepository,
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
    fun provideDimensionRepository(
        context: Context,
        preferencesRepository: IPreferencesRepository
    ): IDimensionRepository = FixedDimensionRepository()

    @Provides
    fun provideSavesRepository(): ISavesRepository = MemorySavesRepository()

    @Provides
    fun provideMinefieldRepository(): IMinefieldRepository = FixedMinefieldRepository()

    @Provides
    fun provideStatsRepository(): IStatsRepository = MemoryStatsRepository()

    @Provides
    fun provideHapticFeedbackInteractor(
        application: Application,
        preferencesRepository: IPreferencesRepository
    ): IHapticFeedbackInteractor = DisabledIHapticFeedbackInteractor()
}
