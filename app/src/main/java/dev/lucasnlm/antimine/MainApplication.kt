package dev.lucasnlm.antimine

import androidx.multidex.MultiDexApplication
import dev.lucasnlm.antimine.common.level.di.LevelModule
import dev.lucasnlm.antimine.common.level.repository.TipRepository
import dev.lucasnlm.antimine.core.di.CommonModule
import dev.lucasnlm.antimine.core.isAndroidTv
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.di.AppModule
import dev.lucasnlm.antimine.di.ViewModelModule
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.di.ExternalModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

open class MainApplication : MultiDexApplication() {
    private val analyticsManager: IAnalyticsManager by inject()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val featureFlagManager: IFeatureFlagManager by inject()
    private val adsManager: IAdsManager by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(applicationContext)
            modules(AppModule, CommonModule, ExternalModule, LevelModule, ViewModelModule)
        }

        analyticsManager.apply {
            setup(applicationContext, mapOf())
            sentEvent(Analytics.Open)
        }

        if (featureFlagManager.isFoos) {
            preferencesRepository.setPremiumFeatures(true)
        } else {
            adsManager.start(this)
        }

        if (applicationContext.isAndroidTv()) {
            preferencesRepository.useTheme(16L)
        }
    }
}
