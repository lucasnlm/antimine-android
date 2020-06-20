package dev.lucasnlm.antimine.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lucasnlm.antimine.instant.InstantAppManager

@Module
@InstallIn(ApplicationComponent::class)
class AppModule {
    @Provides
    fun provideInstantAppManager(
        @ApplicationContext context: Context
    ): InstantAppManager = InstantAppManager(context)
}
