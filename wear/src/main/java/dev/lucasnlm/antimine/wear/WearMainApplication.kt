package dev.lucasnlm.antimine.wear

import androidx.multidex.MultiDexApplication
import com.badlogic.gdx.utils.GdxNativesLoader
import com.google.android.material.color.DynamicColors
import dev.lucasnlm.antimine.common.io.di.CommonIoModule
import dev.lucasnlm.antimine.common.level.di.LevelModule
import dev.lucasnlm.antimine.core.di.CommonModule
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.repository.Themes
import dev.lucasnlm.antimine.wear.di.AppModule
import dev.lucasnlm.antimine.wear.di.ViewModelModule
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.di.ExternalModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

open class WearMainApplication : MultiDexApplication() {
    private val analyticsManager: AnalyticsManager by inject()
    private val preferencesRepository: PreferencesRepository by inject()

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        GdxNativesLoader.load()

        stopKoin()
        startKoin {
            androidContext(applicationContext)
            modules(AppModule, CommonModule, CommonIoModule, ExternalModule, LevelModule, ViewModelModule)
        }

        predefineDarkTheme()

        if (!preferencesRepository.hasCustomControlStyle()) {
            preferencesRepository.useControlStyle(ControlStyle.FastFlag)
        }

        analyticsManager.apply {
            setup(applicationContext, mapOf())
            sentEvent(Analytics.Open)
        }

        preferencesRepository.setPremiumFeatures(true)
    }

    private fun predefineDarkTheme() {
        if (preferencesRepository.themeId() == null) {
            preferencesRepository.useTheme(Themes.darkTheme().id)
        }
    }
}
