package dev.lucasnlm.antimine

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp
import dev.lucasnlm.antimine.core.analytics.IAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.models.Analytics
import javax.inject.Inject

@HiltAndroidApp
open class MainApplication : MultiDexApplication() {
    @Inject
    lateinit var analyticsManager: IAnalyticsManager

    override fun onCreate() {
        super.onCreate()
        analyticsManager.apply {
            setup(applicationContext, mapOf())
            sentEvent(Analytics.Open)
        }
    }
}
