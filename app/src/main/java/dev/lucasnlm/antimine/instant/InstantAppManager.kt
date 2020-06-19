package dev.lucasnlm.antimine.instant

import android.app.Activity
import android.content.Context
import android.content.Intent
import dev.lucasnlm.external.InstantAppWrapper

interface InstantAppManageable {
    fun isEnabled(): Boolean
    fun isNotEnabled(): Boolean
    fun showInstallPrompt(activity: Activity, intent: Intent?, requestCode: Int, referrer: String?): Boolean
}

class InstantAppManager(
    private val context: Context
) : InstantAppManageable {
    override fun isEnabled(): Boolean = InstantAppWrapper().isEnabled(context)

    override fun isNotEnabled(): Boolean = isEnabled().not()

    override fun showInstallPrompt(activity: Activity, intent: Intent?, requestCode: Int, referrer: String?): Boolean =
        InstantAppWrapper().showInstallPrompt(activity, intent, requestCode, referrer)
}
