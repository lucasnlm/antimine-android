package dev.lucasnlm.external

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdMobAdsManager(
    private val context: Context,
    private val crashReporter: ICrashReporter,
    private val featureFlagManager: FeatureFlagManager,
) : IAdsManager {
    private var rewardedAd: RewardedAd? = null
    private var secondRewardedAd: RewardedAd? = null

    private var interstitialAd: InterstitialAd? = null
    private var secondInterstitialAd: InterstitialAd? = null

    private var failErrorCause: String? = null
    private var lastShownAd = 0L

    private var initialized = false

    override fun isAvailable(): Boolean {
        return initialized
    }

    override fun start(context: Context) {
        if (!initialized) {
            initialized = true

            MobileAds.initialize(context) {
                val providerCount = it.adapterStatusMap.count { provider ->
                    provider.value.initializationState == AdapterStatus.State.READY
                }

                if (providerCount != 0) {
                    preloadAds()
                } else {
                    initialized = false
                }
            }
        }
    }

    private fun loadRewardAd() {
        var rewardedAdRetry = 0
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            Ads.RewardAd,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failErrorCause = adError.message
                    rewardedAd = null

                    if (rewardedAdRetry < 3) {
                        rewardedAdRetry++
                        loadRewardAd()
                    }
                }

                override fun onAdLoaded(result: RewardedAd) {
                    rewardedAd = result
                    rewardedAdRetry = 0
                }
            },
        )
    }

    private fun loadSecondRewardAd() {
        var rewardedAdRetry = 0
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            Ads.SecondRewardAd,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failErrorCause = adError.message
                    secondRewardedAd = null

                    if (rewardedAdRetry < 3) {
                        rewardedAdRetry++
                        loadRewardAd()
                    }
                }

                override fun onAdLoaded(result: RewardedAd) {
                    secondRewardedAd = result
                    rewardedAdRetry = 0
                }
            },
        )
    }

    private fun loadInterstitialAd() {
        var interstitialAdRetry = 0
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            Ads.InterstitialAd,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failErrorCause = adError.message
                    interstitialAd = null

                    if (interstitialAdRetry < 3) {
                        loadInterstitialAd()
                        interstitialAdRetry++
                    }
                }

                override fun onAdLoaded(result: InterstitialAd) {
                    interstitialAd = result
                    interstitialAdRetry = 0
                }
            },
        )
    }

    private fun loadSecondInterstitialAd() {
        var interstitialAdRetry = 0
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            Ads.SecondInterstitialAd,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failErrorCause = adError.message
                    secondInterstitialAd = null

                    if (interstitialAdRetry < 3) {
                        loadInterstitialAd()
                        interstitialAdRetry++
                    }
                }

                override fun onAdLoaded(result: InterstitialAd) {
                    secondInterstitialAd = result
                    interstitialAdRetry = 0
                }
            },
        )
    }

    private fun preloadAds() {
        loadRewardAd()
        loadSecondRewardAd()
        loadInterstitialAd()
        loadSecondInterstitialAd()
    }

    override fun showRewardedAd(
        activity: Activity,
        skipIfFrequent: Boolean,
        onRewarded: (() -> Unit)?,
        onFail: (() -> Unit)?,
    ) {
        if (skipIfFrequent && (System.currentTimeMillis() - lastShownAd < Ads.MIN_FREQUENCY)) {
            onRewarded?.invoke()
        } else {
            val rewardedAd = this.rewardedAd
            val secondRewardedAd = this.secondRewardedAd

            when {
                secondRewardedAd != null -> {
                    secondRewardedAd.show(activity) {
                        if (!activity.isFinishing) {
                            lastShownAd = System.currentTimeMillis()
                            onRewarded?.invoke()
                            loadSecondRewardAd()
                        }
                    }
                    loadSecondRewardAd()
                }
                rewardedAd != null -> {
                    rewardedAd.show(activity) {
                        if (!activity.isFinishing) {
                            lastShownAd = System.currentTimeMillis()
                            onRewarded?.invoke()
                            loadRewardAd()
                        }
                    }
                    loadRewardAd()
                }
                else -> {
                    val message = failErrorCause?.let { "Fail to load Ad\n$it" } ?: "Fail to load Ad"
                    crashReporter.sendError(message)
                    onFail?.invoke()
                }
            }
        }
    }

    override fun showInterstitialAd(
        activity: Activity,
        onDismiss: (() -> Unit),
        onError: (() -> Unit)?,
    ) {
        secondInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadSecondInterstitialAd()
                onDismiss.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                crashReporter.sendError(
                    listOf(
                        "Fail to show InterstitialAd",
                        "Code: ${adError.code}",
                        "Message: ${adError.message}",
                        "Cause: ${adError.cause}",
                    ).joinToString("\n"),
                )
                (onError ?: onDismiss).invoke()
            }

            override fun onAdShowedFullScreenContent() {
                // Empty
            }
        }

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadInterstitialAd()
                onDismiss.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                crashReporter.sendError(
                    listOf(
                        "Fail to show InterstitialAd",
                        "Code: ${adError.code}",
                        "Message: ${adError.message}",
                        "Cause: ${adError.cause}",
                    ).joinToString("\n"),
                )
                (onError ?: onDismiss).invoke()
            }

            override fun onAdShowedFullScreenContent() {
                // Empty
            }
        }

        if (interstitialAd == null && secondInterstitialAd == null) {
            (onError ?: onDismiss).invoke()
        } else {
            (secondInterstitialAd ?: interstitialAd)?.show(activity)
        }
    }

    private fun getHexBanner(): View {
        return AppCompatImageView(context).apply {
            setImageResource(R.drawable.hex_banner)
            setOnClickListener {
                val packageName = "dev.lucasnlm.hexo"
                try {
                    val uri = "market://details?id=$packageName"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    val url = "https://play.google.com/store/apps/details?id=$packageName"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    override fun createBannerAd(context: Context): View? {
        return if (featureFlagManager.isHexBannerEnabled) {
            getHexBanner()
        } else {
            val adRequest = AdRequest.Builder().build()
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = Ads.BannerAd
                loadAd(adRequest)
            }
        }
    }
}
