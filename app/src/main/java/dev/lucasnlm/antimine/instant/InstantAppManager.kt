package dev.lucasnlm.antimine.instant

import android.app.Activity
import android.content.Context
import android.content.Intent
import dev.lucasnlm.external.InstantAppWrapper

class InstantAppManager(
    private val context: Context
) {
    fun isEnabled(): Boolean = InstantAppWrapper().isEnabled(context)

    fun isNotEnabled(): Boolean = isEnabled().not()

    fun showInstallPrompt(activity: Activity, intent: Intent?, requestCode: Int, referrer: String?) =
        InstantAppWrapper().showInstallPrompt(activity, intent, requestCode, referrer)
}
