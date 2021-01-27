package dev.lucasnlm.external

class CrashReporter : ICrashReporter {
    override fun sendError(message: String) {
        // FOSS build doesn't log errors.
    }
}
