package dev.lucasnlm.antimine.wear

import android.app.Application
import dev.lucasnlm.antimine.common.level.di.LevelModule
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.di.CommonModule
import dev.lucasnlm.antimine.wear.di.AppModule
import dev.lucasnlm.antimine.wear.di.ViewModelModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

open class MainApplication : Application() {
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
