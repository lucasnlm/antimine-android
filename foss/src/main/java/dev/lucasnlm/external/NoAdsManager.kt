package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.view.View

class NoAdsManager : AdsManager {
    override fun start(context: Context) {}

    override fun showRewardedAd(
        activity: Activity,
        onStart: (() -> Unit)?,
        onRewarded: (() -> Unit)?,
        onFail: (() -> Unit)?,
    ) {
        // Empty
    }

    override fun isAvailable(): Boolean = false

    override fun showInterstitialAd(
        activity: Activity,
        onStart: (() -> Unit)?,
        onDismiss: () -> Unit,
        onError: (() -> Unit)?,
    ) {
        // Empty
    }

    override fun createBannerAd(
        context: Context,
        onError: (() -> Unit)?,
    ): View? {
        return null
    }
}
