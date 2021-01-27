package dev.lucasnlm.external

import android.app.Activity
import android.content.Context

class AdsManager(
    private val crashReporter: ICrashReporter,
) : IAdsManager {
    override fun start(context: Context) { }

    override fun requestRewardedAd(
        activity: Activity,
        onRewarded: (() -> Unit)?,
        onFail: (() -> Unit)?
    ) {}
}
