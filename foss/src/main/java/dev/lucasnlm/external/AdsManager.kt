package dev.lucasnlm.external

import android.app.Activity
import android.content.Context

class AdsManager : IAdsManager {
    override fun start(context: Context) { }

    override fun isReady(): Boolean = false

    override fun requestRewarded(
        activity: Activity,
        adUnitId: String,
        onRewarded: (() -> Unit)?,
        onFail: (() -> Unit)?
    ) {}
}
