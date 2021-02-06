package dev.lucasnlm.external

import android.app.Activity
import android.content.Context

class UnityAdsManager : IAdsManager {
    override fun start(context: Context) {
        // FOSS doesn't have ads
    }

    override fun requestRewardedAd(activity: Activity, onRewarded: (() -> Unit)?, onFail: (() -> Unit)?) {
        // Nothing
    }
}
