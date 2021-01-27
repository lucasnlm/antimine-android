package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdsManager(
    private val crashReporter: ICrashReporter,
) : IAdsManager {
    private var rewardedAd: RewardedAd? = null
    private var failErrorCause: String? = null

    override fun start(context: Context) {
        MobileAds.initialize(context) {
            preloadAds(context)
        }
    }

    private fun preloadAds(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context, Ads.RewardsAds, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failErrorCause = adError.message
                    rewardedAd = null
                }

                override fun onAdLoaded(result: RewardedAd) {
                    rewardedAd = result
                }
            }
        )
    }

    override fun requestRewardedAd(
        activity: Activity,
        onRewarded: (() -> Unit)?,
        onFail: (() -> Unit)?
    ) {
        val rewardedAd = this.rewardedAd
        if (rewardedAd != null) {
            rewardedAd.show(activity) {
                if (!activity.isFinishing) {
                    onRewarded?.invoke()
                    preloadAds(activity)
                }
            }
        } else {
            val message = failErrorCause?.let { "Fail to load Ad\n$it" } ?: "Fail to load Ad"
            crashReporter.sendError(message)
            onFail?.invoke()
        }
    }
}
