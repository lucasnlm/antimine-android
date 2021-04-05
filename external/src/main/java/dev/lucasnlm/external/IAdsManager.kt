package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.view.View

interface IAdsManager {
    fun start(context: Context)

    fun showRewardedAd(
        activity: Activity,
        skipIfFrequent: Boolean,
        onRewarded: (() -> Unit)?,
        onFail: (() -> Unit)?,
    )

    fun showInterstitialAd(
        activity: Activity,
        onDismiss: (() -> Unit),
        onError: (() -> Unit)? = null,
    )

    fun createBannerAd(context: Context): View?
}

object Ads {
    const val RewardsAds = "ca-app-pub-3940256099942544/5224354917"
    const val InterstitialAd = "ca-app-pub-3940256099942544/1033173712"
    const val BannerAd = "ca-app-pub-3940256099942544/6300978111"
    const val MIN_FREQUENCY = 60 * 1000L
}
