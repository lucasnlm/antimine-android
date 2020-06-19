package dev.lucasnlm.antimine.di

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackInteractor
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.mocks.MockDimensionRepository
import dev.lucasnlm.antimine.mocks.MockHapticFeedbackInteractor
import dev.lucasnlm.antimine.mocks.MockMinefieldRepository

@Module
@InstallIn(ActivityComponent::class)
class TestLevelModule {
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
    fun provideDimensionRepository(): IDimensionRepository = MockDimensionRepository()

    @Provides
    fun provideMinefieldRepository(): IMinefieldRepository = MockMinefieldRepository()

    @Provides
    fun provideHapticFeedbackInteractor(): IHapticFeedbackInteractor = MockHapticFeedbackInteractor()
}
