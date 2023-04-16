package dev.lucasnlm.external

class CrashReporterImpl : CrashReporter {
    override fun sendError(message: String) {
        // FOSS build doesn't log errors.
    }
}
