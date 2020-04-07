package dev.lucasnlm.antimine

import android.app.Application
import dev.lucasnlm.antimine.di.AppModule
import dev.lucasnlm.antimine.di.TestLevelModule

class TestApplication : MainApplication() {

    override fun appModule(application: Application) = AppModule(application)

    override fun levelModule(application: Application) = TestLevelModule(application)
}
