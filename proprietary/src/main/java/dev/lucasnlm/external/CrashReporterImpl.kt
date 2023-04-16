package dev.lucasnlm.external

import com.google.firebase.crashlytics.FirebaseCrashlytics

class CrashReporterImpl : CrashReporter {
    override fun sendError(message: String) {
        FirebaseCrashlytics.getInstance().recordException(Exception(message))
    }
}
