package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdMobAdsManager(
    private val context: Context,
    private val crashReporter: ICrashReporter,
) : IAdsManager {
    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null

    private var failErrorCause: String? = null
    private var lastShownAd = 0L

    private var rewardedAdRetry = 0
    private var interstitialAdRetry = 0

    override fun start(context: Context) {
        if (rewardedAd == null) {
            MobileAds.initialize(context) {
                preloadAds()
            }
        } else {
            preloadAds()
        }
    }

    private fun loadRewardsAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context, Ads.RewardsAds, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failErrorCause = adError.message
                    rewardedAd = null

                    if (rewardedAdRetry < 3) {
                        rewardedAdRetry++
                        loadRewardsAd()
                    }
                }

                override fun onAdLoaded(result: RewardedAd) {
                    rewardedAd = result
                    rewardedAdRetry = 0
                }
            }
        )
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context, Ads.InterstitialAd, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failErrorCause = adError.message
                    rewardedAd = null

                    if (interstitialAdRetry < 3) {
                        loadInterstitialAd()
                        interstitialAdRetry++
                    }
                }

                override fun onAdLoaded(result: InterstitialAd) {
                    interstitialAd = result
                    interstitialAdRetry = 0
                }
            }
        )
    }

    private fun preloadAds() {
        loadRewardsAd()
        loadInterstitialAd()
    }

    override fun showRewardedAd(
        activity: Activity,
        skipIfFrequent: Boolean,
        onRewarded: (() -> Unit)?,
        onFail: (() -> Unit)?
    ) {
        if (skipIfFrequent && (System.currentTimeMillis() - lastShownAd < Ads.MIN_FREQUENCY)) {
            onRewarded?.invoke()
        } else {
            val rewardedAd = this.rewardedAd
            if (rewardedAd != null) {
                rewardedAd.show(activity) {
                    if (!activity.isFinishing) {
                        lastShownAd = System.currentTimeMillis()
                        onRewarded?.invoke()
                        preloadAds()
                    }
                }
                loadRewardsAd()
            } else {
                val message = failErrorCause?.let { "Fail to load Ad\n$it" } ?: "Fail to load Ad"
                crashReporter.sendError(message)
                onFail?.invoke()
            }
        }
    }

    override fun showInterstitialAd(
        activity: Activity,
        onDismiss: (() -> Unit),
        onError: (() -> Unit)?,
    ) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadInterstitialAd()
                onDismiss.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                adError?.let {
                    crashReporter.sendError(
                        "Fail to show InterstitialAd \nCode:${it.code} \nMessage: ${it.message} \nCause: ${it.cause}"
                    )
                }
                (onError ?: onDismiss).invoke()
            }

            override fun onAdShowedFullScreenContent() {
                // Empty
            }
        }

        if (interstitialAd == null) {
            (onError ?: onDismiss).invoke()
        } else {
            interstitialAd?.show(activity)
        }
    }

    override fun createBannerAd(context: Context): View? {
        val adRequest = AdRequest.Builder().build()
        return AdView(context).apply {
            adSize = AdSize.SMART_BANNER
            adUnitId = Ads.BannerAd
            loadAd(adRequest)
        }
    }
}
