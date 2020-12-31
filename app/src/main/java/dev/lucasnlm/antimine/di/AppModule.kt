package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.cloud.CloudSaveManagerImpl
import dev.lucasnlm.antimine.support.IapHandler
import dev.lucasnlm.antimine.common.BuildConfig
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.ProdAnalyticsManager
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.share.ShareManager
import dev.lucasnlm.external.AdsManager
import dev.lucasnlm.external.BillingManager
import dev.lucasnlm.external.CloudStorageManager
import dev.lucasnlm.external.FeatureFlagManager
import dev.lucasnlm.external.ExternalAnalyticsWrapper
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.ICloudStorageManager
import dev.lucasnlm.external.IFeatureFlagManager
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

    single { CloudStorageManager() } bind ICloudStorageManager::class

    single { ShareManager(get(), get()) }

    single { IapHandler(get(), get(), get()) }

    single { CloudSaveManagerImpl(get(), get(), get(), get()) } bind CloudSaveManager::class

    single { FeatureFlagManager() } bind IFeatureFlagManager::class

    single {
        if (BuildConfig.DEBUG) {
            DebugAnalyticsManager()
        } else {
            ProdAnalyticsManager(ExternalAnalyticsWrapper(get()))
        }
    } bind IAnalyticsManager::class
}
