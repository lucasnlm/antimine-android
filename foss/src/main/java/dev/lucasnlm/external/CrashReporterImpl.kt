package dev.lucasnlm.external

import android.app.Application
import dev.lucasnlm.antimine.i18n.R
import org.acra.BuildConfig
import org.acra.config.limiter
import org.acra.config.mailSender
import org.acra.config.toast
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

class CrashReporterImpl : CrashReporter {
    override fun sendError(message: String) {
        // FOSS build doesn't log errors.
    }

    override fun start(application: Application) {
        val context = application.applicationContext
        application.initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON

            mailSender {
                subject = "[antimine] crash report"
                mailTo = "me@lucasnlm.dev"
                reportFileName = "report.txt"
            }

            toast {
                text = context.getString(R.string.acra_toast_text)
            }

            limiter {
                // No changes
            }
        }
    }
}
