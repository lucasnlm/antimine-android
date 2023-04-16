package dev.lucasnlm.external

import android.app.Activity

class InAppUpdateManagerImpl : InAppUpdateManager {
    override fun checkUpdate(activity: Activity) {
        // F-droid store doesn't support In-App update.
    }
}
