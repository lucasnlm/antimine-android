package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.view.View

class NoAdsManager : IAdsManager {
    override fun start(context: Context) {}

    override fun showRewardedAd(
        activity: Activity,
        skipIfFrequent: Boolean,
        onRewarded: (() -> Unit)?,
        onFail: (() -> Unit)?
    ) {
        // Empty
    }

    override fun showInterstitialAd(activity: Activity, onDismiss: () -> Unit, onError: (() -> Unit)?) {
        // Empty
    }

    override fun createBannerAd(context: Context): View? {
        return null
    }
}
