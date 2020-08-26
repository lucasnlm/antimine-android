package dev.lucasnlm.external

import android.app.Activity

interface IBillingManager {
    fun start(unlockAppListener: UnlockAppListener)
    fun isEnabled(): Boolean
    suspend fun charge(activity: Activity)
}

interface UnlockAppListener {
    fun onLockStatusChanged(status: Boolean, isFreeUnlock: Boolean? = null)

    fun showFailToConnectFeedback()
}
