package dev.lucasnlm.antimine.wear.di

import dev.lucasnlm.antimine.common.BuildConfig
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.ProdAnalyticsManager
import dev.lucasnlm.external.AdsManager
import dev.lucasnlm.external.BillingManager
import dev.lucasnlm.external.ExternalAnalyticsWrapper
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.IInstantAppManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.IReviewWrapper
import dev.lucasnlm.external.InstantAppManager
import dev.lucasnlm.external.PlayGamesManager
import dev.lucasnlm.external.ReviewWrapper
import org.koin.dsl.bind
import org.koin.dsl.module

val AppModule = module {
    single { InstantAppManager() } bind IInstantAppManager::class

    single { BillingManager(get()) } bind IBillingManager::class

    single { AdsManager() } bind IAdsManager::class

    single { PlayGamesManager(get()) } bind IPlayGamesManager::class

    single { ReviewWrapper() } bind IReviewWrapper::class

    single {
        if (BuildConfig.DEBUG) {
            DebugAnalyticsManager()
        } else {
            ProdAnalyticsManager(ExternalAnalyticsWrapper(get()))
        }
    } bind IAnalyticsManager::class
}
