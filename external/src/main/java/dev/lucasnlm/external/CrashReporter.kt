package dev.lucasnlm.external

import android.app.Application

interface CrashReporter {
    fun sendError(message: String)

    fun start(application: Application)
}
