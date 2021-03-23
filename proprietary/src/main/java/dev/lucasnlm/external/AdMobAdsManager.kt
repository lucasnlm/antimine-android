package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdMobAdsManager(
    private val context: Context,
    private val crashReporter: ICrashReporter,
) : IAdsManager {
    private var rewardedAd: RewardedAd? = null
    private var failErrorCause: String? = null
    private var lastShownAd = 0L

    override fun start(context: Context) {
        if (rewardedAd == null) {
            MobileAds.initialize(context) {
                preloadAds(context)
            }
        } else {
            preloadAds(context)
        }
    }

    override fun loadAd() {
        this.rewardedAd = null
        preloadAds(context)
    }

    private fun preloadAds(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context, Ads.RewardsAds, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failErrorCause = adError.message
                    rewardedAd = null
                    loadAd()
                }

                override fun onAdLoaded(result: RewardedAd) {
                    rewardedAd = result
                }
            }
        )
    }

    override fun showRewardedAd(
        activity: Activity,
        skipIfFrequent: Boolean,
        onRewarded: (() -> Unit)?,
        onFail: (() -> Unit)?
    ) {
        if (System.currentTimeMillis() - lastShownAd < Ads.MIN_FREQUENCY) {
            onRewarded?.invoke()
        } else {
            val rewardedAd = this.rewardedAd
            if (rewardedAd != null) {
                rewardedAd.show(activity) {
                    if (!activity.isFinishing) {
                        lastShownAd = System.currentTimeMillis()
                        onRewarded?.invoke()
                        preloadAds(activity)
                    }
                }
                loadAd()
            } else {
                val message = failErrorCause?.let { "Fail to load Ad\n$it" } ?: "Fail to load Ad"
                crashReporter.sendError(message)
                onFail?.invoke()
            }
        }
    }
}
