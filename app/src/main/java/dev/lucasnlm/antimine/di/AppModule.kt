package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.cloud.CloudSaveManagerImpl
import dev.lucasnlm.antimine.common.BuildConfig
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.ProdAnalyticsManager
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.support.IapHandler
import dev.lucasnlm.external.ExternalAnalyticsWrapper
import dev.lucasnlm.external.IAnalyticsManager
import org.koin.dsl.bind
import org.koin.dsl.module

val AppModule = module {
    single { IapHandler(get(), get(), get()) }

    single { CloudSaveManagerImpl(get(), get(), get(), get()) } bind CloudSaveManager::class

    single {
        if (BuildConfig.DEBUG) {
            DebugAnalyticsManager()
        } else {
            ProdAnalyticsManager(ExternalAnalyticsWrapper(get()))
        }
    } bind IAnalyticsManager::class
}
