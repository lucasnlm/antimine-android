package dev.lucasnlm.external

interface CrashReporter {
    fun sendError(message: String)
}
