package dev.lucasnlm.antimine.common.level.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.repository.DimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.HapticFeedbackInteractor
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackInteractor
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

@Module
@InstallIn(ActivityComponent::class)
open class LevelModule() {
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
        @ApplicationContext context: Context,
        preferencesRepository: IPreferencesRepository
    ): IDimensionRepository =
        DimensionRepository(context, preferencesRepository)

    @Provides
    open fun provideMinefieldRepository(): IMinefieldRepository = MinefieldRepository()

    @Provides
    open fun provideHapticFeedbackInteractor(
        @ApplicationContext context: Context,
        preferencesRepository: IPreferencesRepository
    ): IHapticFeedbackInteractor =
        HapticFeedbackInteractor(context, preferencesRepository)
}
