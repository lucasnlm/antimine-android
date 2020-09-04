package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdsManager : IAdsManager {
    private var unlockTheme: RewardedAd? = null
    private val rewardedAdId = Ads.RewardsAds

    override fun start(context: Context) {
        MobileAds.initialize(context) {
            unlockTheme = loadRewardedAd(context)
        }
    }

    private fun loadRewardedAd(context: Context): RewardedAd {
        return RewardedAd(context, rewardedAdId).apply {
            val adLoadCallback = object : RewardedAdLoadCallback() {
                override fun onRewardedAdLoaded() {
                    // Loaded
                }

                override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
                    // Ad failed to load.
                }
            }

            loadAd(AdRequest.Builder().build(), adLoadCallback)
        }
    }

    override fun isReady(): Boolean {
        return unlockTheme != null
    }

    override fun requestRewarded(
        activity: Activity,
        adUnitId: String,
        onRewarded: (() -> Unit)?,
        onFail: (() -> Unit)?
    ) {
        if (isReady()) {
            val context = activity.applicationContext

            unlockTheme?.let {
                val adCallback = object : RewardedAdCallback() {
                    override fun onRewardedAdOpened() {
                        // Ad opened
                    }

                    override fun onRewardedAdClosed() {
                        // Ad closed
                    }

                    override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                        onRewarded?.invoke()
                    }

                    override fun onRewardedAdFailedToShow(adError: AdError) {
                        onFail?.invoke()
                    }
                }

                unlockTheme = loadRewardedAd(context)

                if (!activity.isFinishing) {
                    it.show(activity, adCallback)
                }
            }
        }
    }
}
