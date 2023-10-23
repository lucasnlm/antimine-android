package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
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
    private val crashReporter: CrashReporter,
    private val scope: CoroutineScope,
) : AdsManager {
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
                val providerCount =
                    it.adapterStatusMap.count { provider ->
                        provider.value.initializationState == AdapterStatus.State.READY
                    }

                if (providerCount != 0) {
                    scope.launch(Dispatchers.Main) {
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
            Ads.REWARD_AD,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failErrorCause = adError.message
                    rewardedAd = null

                    if (rewardedAdRetry < MAX_RETRY) {
                        rewardedAdRetry++
                        scope.launch(Dispatchers.Main) {
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
            Ads.SECOND_REWARD_AD,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failErrorCause = adError.message
                    secondRewardedAd = null

                    if (rewardedAdRetry < MAX_RETRY) {
                        rewardedAdRetry++
                        scope.launch(Dispatchers.Main) {
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
            Ads.INTERSTITIAL_AD,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failErrorCause = adError.message
                    interstitialAd = null

                    if (interstitialAdRetry < MAX_RETRY) {
                        scope.launch(Dispatchers.Main) {
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
            Ads.SECOND_INTERSTITIAL_AD,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failErrorCause = adError.message
                    secondInterstitialAd = null

                    if (interstitialAdRetry < MAX_RETRY) {
                        scope.launch(Dispatchers.Main) {
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
        onStart: (() -> Unit)?,
        onRewarded: (() -> Unit)?,
        onFail: (() -> Unit)?,
    ) {
        val rewardedAd = this.rewardedAd
        val secondRewardedAd = this.secondRewardedAd

        when {
            secondRewardedAd != null -> {
                onStart?.invoke()
                secondRewardedAd.show(activity) {
                    if (!activity.isFinishing) {
                        lastShownAd = System.currentTimeMillis()
                        onRewarded?.invoke()
                        scope.launch(Dispatchers.Main) {
                            loadSecondRewardAd()
                        }
                    }
                }
            }
            rewardedAd != null -> {
                onStart?.invoke()
                rewardedAd.show(activity) {
                    if (!activity.isFinishing) {
                        lastShownAd = System.currentTimeMillis()
                        onRewarded?.invoke()
                        scope.launch(Dispatchers.Main) {
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

    override fun showInterstitialAd(
        activity: Activity,
        onStart: (() -> Unit)?,
        onDismiss: (() -> Unit),
        onError: (() -> Unit)?,
    ) {
        secondInterstitialAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
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

        interstitialAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    scope.launch(Dispatchers.Main) {
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

    override fun createBannerAd(
        context: Context,
        onError: (() -> Unit)?,
    ): View {
        val adRequest = AdRequest.Builder().build()
        return AdView(context).apply {
            setAdSize(AdSize.FULL_BANNER)
            adUnitId = Ads.BANNER_AD
            loadAd(adRequest)
            adListener =
                object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        super.onAdFailedToLoad(error)
                        onError?.invoke()
                    }
                }
        }
    }

    companion object {
        const val MAX_RETRY = 3
        const val RETRY_DELAY_MS = 2000L
    }
}
