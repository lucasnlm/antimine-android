package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.instantapps.InstantApps

class InstantAppManager : IInstantAppManager {
    override fun isEnabled(context: Context): Boolean {
        return InstantApps.getPackageManagerCompat(context).isInstantApp
    }

    override fun showInstallPrompt(activity: Activity, intent: Intent?, requestCode: Int, referrer: String?): Boolean =
        InstantApps.showInstallPrompt(activity, intent, requestCode, referrer)
}
