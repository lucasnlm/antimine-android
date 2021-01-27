package dev.lucasnlm.external

import com.google.firebase.crashlytics.FirebaseCrashlytics

class CrashReporter : ICrashReporter {
    override fun sendError(message: String) {
        FirebaseCrashlytics.getInstance().recordException(Exception(message))
    }
}
