package dev.lucasnlm.antimine

import androidx.multidex.MultiDexApplication
import com.badlogic.gdx.utils.GdxNativesLoader
import dev.lucasnlm.antimine.common.level.di.LevelModule
import dev.lucasnlm.antimine.core.di.CommonModule
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.di.AppModule
import dev.lucasnlm.antimine.di.ViewModelModule
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.support.IapHandler
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.di.ExternalModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

open class MainApplication : MultiDexApplication() {
    private val appScope: CoroutineScope by inject()
    private val analyticsManager: IAnalyticsManager by inject()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val featureFlagManager: IFeatureFlagManager by inject()
    private val adsManager: IAdsManager by inject()
    private val iapHandler: IapHandler by inject()

    override fun onCreate() {
        super.onCreate()

        GdxNativesLoader.load()

        stopKoin()
        startKoin {
            androidContext(applicationContext)
            modules(AppModule, CommonModule, ExternalModule, LevelModule, ViewModelModule)
        }

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
            appScope.launch {
                featureFlagManager.refresh()
            }
            adsManager.start(this)
        }
    }
}
