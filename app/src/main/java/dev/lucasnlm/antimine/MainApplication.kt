package dev.lucasnlm.antimine

import androidx.multidex.MultiDexApplication
import dev.lucasnlm.antimine.common.level.di.LevelModule
import dev.lucasnlm.antimine.core.analytics.IAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.models.Analytics
import dev.lucasnlm.antimine.core.di.CommonModule
import dev.lucasnlm.antimine.di.AppModule
import dev.lucasnlm.antimine.di.ViewModelModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

open class MainApplication : MultiDexApplication() {
    private val analyticsManager: IAnalyticsManager by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(applicationContext)
            modules(AppModule, CommonModule, LevelModule, ViewModelModule)
        }

        analyticsManager.apply {
            setup(applicationContext, mapOf())
            sentEvent(Analytics.Open)
        }
    }
}
