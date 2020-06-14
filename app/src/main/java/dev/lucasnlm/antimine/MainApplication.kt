package dev.lucasnlm.antimine

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import dagger.hilt.android.HiltAndroidApp
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.analytics.models.Analytics
import javax.inject.Inject

@HiltAndroidApp
open class MainApplication : Application() {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        analyticsManager.setup(applicationContext, mapOf())
        analyticsManager.sentEvent(Analytics.Open())
    }
}
