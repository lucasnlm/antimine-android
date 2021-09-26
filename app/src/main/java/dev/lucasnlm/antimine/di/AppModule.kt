package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.BuildConfig
import dev.lucasnlm.antimine.cloud.CloudSaveManagerImpl
import dev.lucasnlm.antimine.core.IAppVersionManager
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.ProdAnalyticsManager
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManager
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManagerImpl
import dev.lucasnlm.antimine.support.AppVersionManagerImpl
import dev.lucasnlm.antimine.support.IapHandler
import dev.lucasnlm.external.ExternalAnalyticsWrapper
import dev.lucasnlm.external.IAnalyticsManager
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.bind
import org.koin.dsl.module

val AppModule = module {
    single { IapHandler(get(), get(), get()) }

    single {
        HapticFeedbackManagerImpl(get(), get())
    } bind HapticFeedbackManager::class

    single { CloudSaveManagerImpl(get(), get(), get(), get()) } bind CloudSaveManager::class

    single { AppVersionManagerImpl(BuildConfig.DEBUG, androidApplication()) } bind IAppVersionManager::class

    single {
        if (BuildConfig.DEBUG) {
            DebugAnalyticsManager()
        } else {
            ProdAnalyticsManager(ExternalAnalyticsWrapper(get()))
        }
    } bind IAnalyticsManager::class
}
