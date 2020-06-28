package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.content.Intent

@Suppress("UNUSED_PARAMETER")
class InstantAppWrapper {
    // FOSS build doesn't support Instant App
    fun isEnabled(context: Context): Boolean = false

    fun showInstallPrompt(activity: Activity, intent: Intent?, requestCode: Int, referrer: String?) {
        // Empty
    }
}
