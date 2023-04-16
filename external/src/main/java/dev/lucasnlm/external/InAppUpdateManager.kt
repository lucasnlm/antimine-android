package dev.lucasnlm.external

import android.app.Activity

interface InAppUpdateManager {
    fun checkUpdate(activity: Activity)
}
