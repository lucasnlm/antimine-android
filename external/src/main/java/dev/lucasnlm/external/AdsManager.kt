package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.view.View

interface AdsManager {
    fun start(context: Context)

    fun isAvailable(): Boolean

    fun showRewardedAd(
        activity: Activity,
        onStart: (() -> Unit)? = null,
        onRewarded: (() -> Unit)?,
        onFail: (() -> Unit)?,
    )

    fun showInterstitialAd(
        activity: Activity,
        onStart: (() -> Unit)? = null,
        onDismiss: (() -> Unit),
        onError: (() -> Unit)? = null,
    )

    fun createBannerAd(
        context: Context,
        onError: (() -> Unit)? = null,
    ): View?
}
