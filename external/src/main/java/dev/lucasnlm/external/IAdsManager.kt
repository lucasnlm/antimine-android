package dev.lucasnlm.external

import android.app.Activity
import android.content.Context

interface IAdsManager {
    fun start(context: Context)
    fun isReady(): Boolean
    fun requestRewarded(
        activity: Activity,
        adUnitId: String,
        onRewarded: (() -> Unit)? = null,
        onFail: (() -> Unit)? = null,
    )
}

object Ads {
    const val RewardsAds = "ca-app-pub-3940256099942544/5224354917"
}
