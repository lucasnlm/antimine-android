package dev.lucasnlm.antimine.instant

import android.app.Activity
import android.content.Context
import android.content.Intent
import dev.lucasnlm.external.InstantAppManager

class InstantAppManager(
    private val context: Context
) {
    fun isEnabled(): Boolean = InstantAppManager().isInstantAppSupported(context)

    fun isNotEnabled(): Boolean = isEnabled().not()

    fun showInstallPrompt(activity: Activity, intent: Intent?, requestCode: Int, referrer: String?) =
        InstantAppManager().showInstallPrompt(activity, intent, requestCode, referrer)
}
