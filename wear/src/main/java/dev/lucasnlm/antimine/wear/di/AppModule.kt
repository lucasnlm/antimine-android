package dev.lucasnlm.antimine.wear.di

import dev.lucasnlm.antimine.core.IAppVersionManager
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.ProdAnalyticsManager
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManager
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManagerImpl
import dev.lucasnlm.antimine.wear.BuildConfig
import dev.lucasnlm.antimine.wear.core.AppVersionManagerImpl
import dev.lucasnlm.external.ExternalAnalyticsWrapper
import dev.lucasnlm.external.IAnalyticsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.bind
import org.koin.dsl.module

val AppModule = module {
    factory { CoroutineScope(Dispatchers.Main + SupervisorJob()) }

    single { AppVersionManagerImpl() } bind IAppVersionManager::class

    single {
        HapticFeedbackManagerImpl(get(), get())
    } bind HapticFeedbackManager::class

    single {
        if (BuildConfig.DEBUG) {
            DebugAnalyticsManager()
        } else {
            ProdAnalyticsManager(ExternalAnalyticsWrapper(get()))
        }
    } bind IAnalyticsManager::class
}
