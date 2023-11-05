package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.BuildConfig
import dev.lucasnlm.antimine.cloud.CloudSaveManagerImpl
import dev.lucasnlm.antimine.core.AppVersionManager
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.ProdAnalyticsManager
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManager
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManagerImpl
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.core.repository.DimensionRepositoryImpl
import dev.lucasnlm.antimine.l10n.GameLocaleManager
import dev.lucasnlm.antimine.l10n.GameLocaleManagerImpl
import dev.lucasnlm.antimine.support.AppVersionManagerImpl
import dev.lucasnlm.antimine.support.IapHandler
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.ExternalAnalyticsWrapperImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.bind
import org.koin.dsl.module

val AppModule =
    module {
        factory { CoroutineScope(Dispatchers.Main + SupervisorJob()) }

        single { DimensionRepositoryImpl(get()) } bind DimensionRepository::class

        single { IapHandler(get(), get(), get()) }

        single {
            HapticFeedbackManagerImpl(get(), get())
        } bind HapticFeedbackManager::class

        single { CloudSaveManagerImpl(get(), get(), get(), get(), get()) } bind CloudSaveManager::class

        single { AppVersionManagerImpl(BuildConfig.DEBUG, androidApplication()) } bind AppVersionManager::class

        single { GameLocaleManagerImpl(get()) } bind GameLocaleManager::class

        single {
            if (BuildConfig.DEBUG) {
                DebugAnalyticsManager()
            } else {
                ProdAnalyticsManager(ExternalAnalyticsWrapperImpl(get()))
            }
        } bind AnalyticsManager::class
    }
