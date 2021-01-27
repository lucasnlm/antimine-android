package dev.lucasnlm.external

interface ICrashReporter {
    fun sendError(message: String)
}
