package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.content.Intent

@Suppress("UNUSED_PARAMETER")
class ProprietaryAppWrapper {
    // FOSS build doesn't support Instant App
    fun isInstantAppSupported(context: Context): Boolean = false

    fun isInAppPaymentsSupported(context: Context) = false

    fun showInstallPrompt(
        activity: Activity,
        intent: Intent?,
        requestCode: Int,
        referrer: String?,
    ) {
        // Empty
    }
}
