package dev.lucasnlm.antimine.instant

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.instantapps.InstantApps

class InstantAppManager(
    private val context: Context
) {
    fun isEnabled(): Boolean = InstantApps.getPackageManagerCompat(context).isInstantApp

    fun isNotEnabled(): Boolean = isEnabled().not()

    fun showInstallPrompt(activity: Activity, intent: Intent?, requestCode: Int, referrer: String?) =
        InstantApps.showInstallPrompt(activity, intent, requestCode, referrer)
}
