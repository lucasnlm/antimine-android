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
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackInteractor
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.mocks.MockDimensionRepository
import dev.lucasnlm.antimine.mocks.MockHapticFeedbackInteractor
import dev.lucasnlm.antimine.mocks.MockMinefieldRepository
import dev.lucasnlm.antimine.mocks.MockSavesRepository

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
    ): IDimensionRepository = MockDimensionRepository()

    @Provides
    override fun provideSavesRepository(): ISavesRepository = MockSavesRepository()

    @Provides
    override fun provideMinefieldRepository(): IMinefieldRepository = MockMinefieldRepository()

    @Provides
    override fun provideHapticFeedbackInteractor(
        application: Application,
        preferencesRepository: IPreferencesRepository
    ): IHapticFeedbackInteractor = MockHapticFeedbackInteractor()
}
