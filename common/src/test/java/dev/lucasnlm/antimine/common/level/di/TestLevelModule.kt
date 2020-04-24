package dev.lucasnlm.antimine.common.level.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import dagger.Module
import dagger.Provides
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.common.level.repository.IDimensionRepository
import dev.lucasnlm.antimine.common.level.repository.IMinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.Size
import dev.lucasnlm.antimine.common.level.utils.Clock
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
    ): IDimensionRepository = object : IDimensionRepository {
        override fun areaSize(): Float = 50.0f

        override fun areaSizeWithPadding(): Float = 52.0f

        override fun displaySize(): Size = Size(50 * 15, 50 * 30)

        override fun actionBarSize(): Int = 50
    }

    @Provides
    fun provideSavesRepository(): ISavesRepository = object : ISavesRepository {
        override suspend fun fetchCurrentSave(): Save? = null

        override suspend fun saveGame(save: Save): Long? = null

        override fun setLimit(maxSavesStorage: Int) { }
    }

    @Provides
    fun provideMinefieldRepository(): IMinefieldRepository = object : IMinefieldRepository {
        override fun fromDifficulty(
            difficulty: Difficulty,
            dimensionRepository: IDimensionRepository,
            preferencesRepository: IPreferencesRepository
        ) = Minefield(9, 9, 9)

        override fun randomSeed(): Long = 200
    }

    @Provides
    fun provideHapticFeedbackInteractor(
        application: Application,
        preferencesRepository: IPreferencesRepository
    ): IHapticFeedbackInteractor = object : IHapticFeedbackInteractor {
        override fun toggleFlagFeedback() { }

        override fun explosionFeedback() { }
    }
}
