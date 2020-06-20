package dev.lucasnlm.antimine.di

import android.content.Context
import androidx.lifecycle.MutableLiveData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
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
@InstallIn(ActivityComponent::class)
class TestLevelModule {
    @Provides
    fun provideGameEventObserver(): MutableLiveData<Event> = MutableLiveData()

    @Provides
    fun provideClock(): Clock = Clock()

    @Provides
    fun provideGameViewModelFactory(
        @ApplicationContext context: Context,
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
        context,
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
    fun provideDimensionRepository(): IDimensionRepository = FixedDimensionRepository()

    @Provides
    fun provideSavesRepository(): ISavesRepository = MemorySavesRepository()

    @Provides
    fun provideStatsRepository(): IStatsRepository = MemoryStatsRepository()

    @Provides
    fun provideMinefieldRepository(): IMinefieldRepository = FixedMinefieldRepository()

    @Provides
    fun provideHapticFeedbackInteractor(): IHapticFeedbackInteractor = DisabledHapticFeedbackInteractor()
}
