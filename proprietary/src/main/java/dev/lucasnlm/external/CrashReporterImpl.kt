package dev.lucasnlm.external

import android.app.Application
import com.bugsnag.android.Bugsnag

class CrashReporterImpl : CrashReporter {
    override fun sendError(message: String) {
        // No-op
    }

    override fun start(application: Application) {
        Bugsnag.start(application.applicationContext)
    }
}
