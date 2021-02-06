package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import com.unity3d.ads.IUnityAdsListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAds.FinishState

class UnityAdsManager : IAdsManager, IUnityAdsListener {
    private val placementId: String = "rewardedVideo"
    private var onRewardedCallback: (() -> Unit)? = null

    override fun start(context: Context) {
        UnityAds.addListener(this)
        UnityAds.initialize(context, UNITY_ID, BuildConfig.DEBUG)
    }

    override fun requestRewardedAd(activity: Activity, onRewarded: (() -> Unit)?, onFail: (() -> Unit)?) {
        if (UnityAds.isReady(placementId)) {
            onRewardedCallback = onRewarded
            UnityAds.show(activity, placementId)
        } else {
            onFail?.invoke()
        }
    }

    override fun onUnityAdsReady(placementId: String?) {
        // Nothing
    }

    override fun onUnityAdsStart(placementId: String?) {
        // Nothing
    }

    override fun onUnityAdsFinish(placementId: String?, result: UnityAds.FinishState?) {
        if (result == FinishState.COMPLETED) {
            onRewardedCallback?.invoke()
        }
        onRewardedCallback = null
    }

    override fun onUnityAdsError(error: UnityAds.UnityAdsError?, message: String?) {
        onRewardedCallback = null
    }

    companion object {
        const val UNITY_ID = "3406763"
    }
}
