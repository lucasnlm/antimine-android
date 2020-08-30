package dev.lucasnlm.external

import android.app.Activity
import android.content.Context

interface IAdsManager {
    fun start(context: Context)
    fun isReady(): Boolean
    fun requestRewarded(activity: Activity, adUnitId: String, onRewarded: () -> Unit)
}
