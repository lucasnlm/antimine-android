package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.support.IapHandler
import dev.lucasnlm.antimine.common.BuildConfig
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.IAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.ProdAnalyticsManager
import dev.lucasnlm.antimine.share.ShareManager
import dev.lucasnlm.external.BillingManager
import dev.lucasnlm.external.ExternalAnalyticsWrapper
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.IInstantAppManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.InstantAppManager
import dev.lucasnlm.external.PlayGamesManager
import org.koin.dsl.bind
import org.koin.dsl.module

val AppModule = module {
    single { InstantAppManager() } bind IInstantAppManager::class

    single { BillingManager(get()) } bind IBillingManager::class

    single { PlayGamesManager(get()) } bind IPlayGamesManager::class

    single { ShareManager(get()) }

    single { IapHandler(get(), get()) }

    single {
        if (BuildConfig.DEBUG) {
            DebugAnalyticsManager()
        } else {
            ProdAnalyticsManager(ExternalAnalyticsWrapper(get()))
        }
    } bind IAnalyticsManager::class
}
