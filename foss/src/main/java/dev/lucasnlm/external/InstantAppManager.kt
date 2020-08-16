package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.content.Intent

class InstantAppManager : IInstantAppManager {
    override fun isInstantAppSupported(context: Context): Boolean = false

    override fun isInAppPaymentsSupported(context: Context): Boolean = false

    override fun showInstallPrompt(activity: Activity, intent: Intent?, requestCode: Int, referrer: String?): Boolean = false
}
