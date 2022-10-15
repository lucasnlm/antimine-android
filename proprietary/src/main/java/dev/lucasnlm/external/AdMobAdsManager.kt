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
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdMobAdsManager(
    private val context: Context,
    private val crashReporter: ICrashReporter,
    private val scope: CoroutineScope,
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
                    scope.launch {
                        preloadAds()
                    }
                } else {
                    initialized = false
                }
            }
        }
    }

    private suspend fun loadRewardAd() {
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
                        scope.launch {
                            delay(RETRY_DELAY_MS)
                            loadRewardAd()
                        }
                    }
                }

                override fun onAdLoaded(result: RewardedAd) {
                    rewardedAd = result
                    rewardedAdRetry = 0
                }
            },
        )
    }

    private suspend fun loadSecondRewardAd() {
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
                        scope.launch {
                            delay(RETRY_DELAY_MS)
                            loadSecondRewardAd()
                        }
                    }
                }

                override fun onAdLoaded(result: RewardedAd) {
                    secondRewardedAd = result
                    rewardedAdRetry = 0
                }
            },
        )
    }

    private suspend fun loadInterstitialAd() {
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
                        scope.launch {
                            delay(RETRY_DELAY_MS)
                            loadInterstitialAd()
                            interstitialAdRetry++
                        }
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
                        scope.launch {
                            delay(RETRY_DELAY_MS)
                            loadSecondInterstitialAd()
                            interstitialAdRetry++
                        }
                    }
                }

                override fun onAdLoaded(result: InterstitialAd) {
                    secondInterstitialAd = result
                    interstitialAdRetry = 0
                }
            },
        )
    }

    private suspend fun preloadAds() {
        withContext(Dispatchers.Main) {
            loadRewardAd()
            loadSecondRewardAd()
            loadInterstitialAd()
            loadSecondInterstitialAd()
        }
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
                            scope.launch {
                                loadSecondRewardAd()
                            }
                        }
                    }
                }
                rewardedAd != null -> {
                    rewardedAd.show(activity) {
                        if (!activity.isFinishing) {
                            lastShownAd = System.currentTimeMillis()
                            onRewarded?.invoke()
                            scope.launch {
                                loadRewardAd()
                            }
                        }
                    }
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
                scope.launch {
                    loadInterstitialAd()
                }
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

    override fun createBannerAd(context: Context): View? {
        val adRequest = AdRequest.Builder().build()
        return AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = Ads.BannerAd
            loadAd(adRequest)
        }
    }

    companion object {
        const val RETRY_DELAY_MS = 2000L
    }
}
