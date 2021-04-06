package dev.lucasnlm.external

import android.app.Activity

interface IInAppUpdateManager {
    fun checkUpdate(activity: Activity)
}
