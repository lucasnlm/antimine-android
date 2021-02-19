package dev.lucasnlm.external

import android.app.Activity
import android.content.Context

class NoAdsManager : IAdsManager {
    override fun start(context: Context) {}

    override fun loadAd() {}

    override fun showRewardedAd(
        activity: Activity,
        onRewarded: (() -> Unit)?,
        onFail: (() -> Unit)?
    ) {
    }
}
