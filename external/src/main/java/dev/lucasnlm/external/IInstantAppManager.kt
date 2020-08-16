package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.content.Intent

interface IInstantAppManager {
    fun isInstantAppSupported(context: Context): Boolean
    fun isInAppPaymentsSupported(context: Context): Boolean
    fun showInstallPrompt(activity: Activity, intent: Intent?, requestCode: Int, referrer: String?): Boolean
}
