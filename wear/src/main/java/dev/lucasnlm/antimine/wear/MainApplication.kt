package dev.lucasnlm.antimine.wear

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import dev.lucasnlm.antimine.wear.di.AppModule
import dev.lucasnlm.antimine.wear.di.DaggerAppComponent

class MainApplication : DaggerApplication() {
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerAppComponent.builder()
            .application(this)
            .appModule(AppModule(this))
            .build()
}
