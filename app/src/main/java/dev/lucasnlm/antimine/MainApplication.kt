package dev.lucasnlm.antimine

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import dev.lucasnlm.antimine.common.level.di.LevelModule
import dev.lucasnlm.antimine.core.analytics.AnalyticsManager
import dev.lucasnlm.antimine.core.analytics.models.Analytics
import dev.lucasnlm.antimine.di.AppModule
import dev.lucasnlm.antimine.di.DaggerAppComponent
import javax.inject.Inject

open class MainApplication : DaggerApplication() {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    protected open fun appModule(application: Application) = AppModule(application)

    protected open fun levelModule(application: Application) = LevelModule(application)

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerAppComponent.builder()
            .application(this)
            .appModule(appModule(this))
            .levelModule(levelModule(this))
            .build()

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
