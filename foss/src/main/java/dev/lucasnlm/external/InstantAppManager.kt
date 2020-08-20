package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.content.Intent

class InstantAppManager : IInstantAppManager {
    override fun isEnabled(context: Context): Boolean {
        return false
    }

    override fun showInstallPrompt(activity: Activity, intent: Intent?, requestCode: Int, referrer: String?): Boolean {
        return false
    }
}
