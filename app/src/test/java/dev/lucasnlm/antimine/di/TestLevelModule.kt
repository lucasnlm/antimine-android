package dev.lucasnlm.antimine.di

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
import dev.lucasnlm.antimine.common.level.repository.MemorySavesRepository
import dev.lucasnlm.antimine.common.level.repository.MemoryStatsRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.utils.IHapticFeedbackManager
import dev.lucasnlm.antimine.mocks.DisabledHapticFeedbackManager
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
    fun provideDimensionRepository(): IDimensionRepository = FixedDimensionRepository()

    @Provides
    fun provideSavesRepository(): ISavesRepository = MemorySavesRepository()

    @Provides
    fun provideStatsRepository(): IStatsRepository = MemoryStatsRepository()

    @Provides
    fun provideMinefieldRepository(): IMinefieldRepository = FixedMinefieldRepository()

    @Provides
    fun provideHapticFeedbackInteractor(): IHapticFeedbackManager = DisabledHapticFeedbackManager()
}
