package dev.lucasnlm.antimine.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import dagger.Module
import dagger.Provides
import dev.lucasnlm.antimine.common.level.di.LevelModule
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.common.level.repository.MemorySavesRepository
import dev.lucasnlm.antimine.common.level.repository.MemoryStatsRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackInteractor
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.mocks.DisabledHapticFeedbackInteractor
import dev.lucasnlm.antimine.mocks.FixedDimensionRepository
import dev.lucasnlm.antimine.mocks.FixedMinefieldRepository

@Module
class TestLevelModule(
    application: Application
) : LevelModule(application) {
    @Provides
    override fun provideGameEventObserver(): MutableLiveData<Event> = MutableLiveData()

    @Provides
    override fun provideClock(): Clock = Clock()

    @Provides
    override fun provideGameViewModelFactory(
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
    override fun provideDimensionRepository(
        context: Context,
        preferencesRepository: IPreferencesRepository
    ): IDimensionRepository = FixedDimensionRepository()

    @Provides
    override fun provideSavesRepository(): ISavesRepository = MemorySavesRepository()

    @Provides
    override fun provideStatsRepository(): IStatsRepository = MemoryStatsRepository()

    @Provides
    override fun provideMinefieldRepository(): IMinefieldRepository = FixedMinefieldRepository()

    @Provides
    override fun provideHapticFeedbackInteractor(
        application: Application,
        preferencesRepository: IPreferencesRepository
    ): IHapticFeedbackInteractor = DisabledHapticFeedbackInteractor()
}
