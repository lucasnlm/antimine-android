package dev.lucasnlm.antimine.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lucasnlm.antimine.common.BuildConfig
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.IAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.ProdAnalyticsManager
import dev.lucasnlm.antimine.instant.InstantAppManager
import dev.lucasnlm.external.BillingManager
import dev.lucasnlm.external.ExternalAnalyticsWrapper
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.PlayGamesManager
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun provideInstantAppManager(
        @ApplicationContext context: Context
    ): InstantAppManager = InstantAppManager(context)

    @Singleton
    @Provides
    fun provideBillingManager(
        @ApplicationContext context: Context
    ): IBillingManager = BillingManager(context)

    @Singleton
    @Provides
    fun providePlayGamesManager(
        @ApplicationContext context: Context
    ): IPlayGamesManager = PlayGamesManager(context)

    @Singleton
    @Provides
    fun provideAnalyticsManager(
        @ApplicationContext context: Context
    ): IAnalyticsManager {
        return if (BuildConfig.DEBUG) {
            DebugAnalyticsManager()
        } else {
            ProdAnalyticsManager(ExternalAnalyticsWrapper(context))
        }
    }
}
