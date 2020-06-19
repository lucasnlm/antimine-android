package dev.lucasnlm.antimine.wear.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository

@Module
@InstallIn(ApplicationComponent::class)
class AppModule {
    @Provides
    fun provideSavesRepository(): ISavesRepository = MemorySavesRepository()

    @Provides
    fun provideStatsRepository(): IStatsRepository = MemoryStatsRepository()
}
