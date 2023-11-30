package dev.lucasnlm.antimine

import androidx.multidex.MultiDexApplication
import com.badlogic.gdx.utils.GdxNativesLoader
import com.google.android.material.color.DynamicColors
import dev.lucasnlm.antimine.common.auto.AutoExt.isAndroidAuto
import dev.lucasnlm.antimine.common.io.di.CommonIoModule
import dev.lucasnlm.antimine.common.level.di.LevelModule
import dev.lucasnlm.antimine.core.di.CommonModule
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.di.AppModule
import dev.lucasnlm.antimine.di.ViewModelModule
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.support.IapHandler
import dev.lucasnlm.external.AdsManager
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.CrashReporter
import dev.lucasnlm.external.FeatureFlagManager
import dev.lucasnlm.external.di.ExternalModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

open class MainApplication : MultiDexApplication() {
    private val appScope: CoroutineScope by inject()
    private val analyticsManager: AnalyticsManager by inject()
    private val preferencesRepository: PreferencesRepository by inject()
    private val featureFlagManager: FeatureFlagManager by inject()
    private val adsManager: AdsManager by inject()
    private val iapHandler: IapHandler by inject()
    private val crashReporter: CrashReporter by inject()

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        stopKoin()
        startKoin {
            androidContext(applicationContext)
            modules(AppModule, CommonModule, CommonIoModule, ExternalModule, LevelModule, ViewModelModule)
        }

        crashReporter.start(this)

        appScope.launch {
            iapHandler.start()
        }

        analyticsManager.apply {
            setup(applicationContext, mapOf())
            sentEvent(Analytics.Open)
        }

        if (featureFlagManager.isFoss) {
            preferencesRepository.setPremiumFeatures(true)
        } else {
            if (applicationContext.isAndroidAuto()) {
                preferencesRepository.setPremiumFeatures(true)
            } else {
                adsManager.start(this)
            }
        }

        val lastAppVersion = preferencesRepository.lastAppVersion()
        if (lastAppVersion == null) {
            preferencesRepository.setLastAppVersion(BuildConfig.VERSION_CODE)
        }
    }

    companion object {
        init {
            GdxNativesLoader.load()
        }
    }
}
