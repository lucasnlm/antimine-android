package dev.lucasnlm.antimine.wear.di

import dev.lucasnlm.antimine.core.AppVersionManager
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.ProdAnalyticsManager
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManager
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManagerImpl
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.core.repository.WearDimensionRepositoryImpl
import dev.lucasnlm.antimine.wear.BuildConfig
import dev.lucasnlm.antimine.wear.core.AppVersionManagerImpl
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.ExternalAnalyticsWrapperImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.bind
import org.koin.dsl.module

val AppModule =
    module {
        factory { CoroutineScope(Dispatchers.Main + SupervisorJob()) }

        single { WearDimensionRepositoryImpl(get()) } bind DimensionRepository::class

        single { AppVersionManagerImpl() } bind AppVersionManager::class

        single {
            HapticFeedbackManagerImpl(get(), get())
        } bind HapticFeedbackManager::class

        single {
            if (BuildConfig.DEBUG) {
                DebugAnalyticsManager()
            } else {
                ProdAnalyticsManager(ExternalAnalyticsWrapperImpl(get()))
            }
        } bind AnalyticsManager::class
    }
