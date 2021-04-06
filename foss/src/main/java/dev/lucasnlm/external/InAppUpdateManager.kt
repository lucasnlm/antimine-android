package dev.lucasnlm.external

import android.app.Activity

class InAppUpdateManager : IInAppUpdateManager {
    override fun checkUpdate(activity: Activity) {
        // F-droid store doesn't support In-App update.
    }
}
