package dev.lucasnlm.antimine.di

import dev.lucasnlm.external.BillingManager
import dev.lucasnlm.external.CloudStorageManager
import dev.lucasnlm.external.CrashReporter
import dev.lucasnlm.external.FeatureFlagManager
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.ICloudStorageManager
import dev.lucasnlm.external.ICrashReporter
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.IInAppUpdateManager
import dev.lucasnlm.external.IInstantAppManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.IReviewWrapper
import dev.lucasnlm.external.InAppUpdateManager
import dev.lucasnlm.external.InstantAppManager
import dev.lucasnlm.external.NoAdsManager
import dev.lucasnlm.external.PlayGamesManager
import dev.lucasnlm.external.ReviewWrapper
import org.koin.dsl.bind
import org.koin.dsl.module

val ExternalModule = module {
    single { InstantAppManager() } bind IInstantAppManager::class

    single { BillingManager() } bind IBillingManager::class

    single { PlayGamesManager(get()) } bind IPlayGamesManager::class

    single { ReviewWrapper() } bind IReviewWrapper::class

    single { CloudStorageManager() } bind ICloudStorageManager::class

    single { FeatureFlagManager() } bind IFeatureFlagManager::class

    single { CrashReporter() } bind ICrashReporter::class

    single { NoAdsManager() } bind IAdsManager::class

    single { InAppUpdateManager() } bind IInAppUpdateManager::class
}
