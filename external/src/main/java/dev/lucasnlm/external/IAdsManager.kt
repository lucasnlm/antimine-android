package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.view.View

interface IAdsManager {
    fun start(context: Context)

    fun isAvailable(): Boolean

    fun showRewardedAd(
        activity: Activity,
        skipIfFrequent: Boolean,
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

object Ads {
    const val RewardAd = "ca-app-pub-3940256099942544/5224354917"
    const val SecondRewardAd = "ca-app-pub-3940256099942544/5224354917"
    const val InterstitialAd = "ca-app-pub-3940256099942544/1033173712"
    const val SecondInterstitialAd = "ca-app-pub-3940256099942544/1033173712"
    const val BannerAd = "ca-app-pub-3940256099942544/6300978111"
    const val MIN_FREQUENCY = 60 * 1000L
}
