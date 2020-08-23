package dev.lucasnlm.external

import android.app.Activity

interface IBillingManager {
    fun start(unlockAppListener: UnlockAppListener)
    suspend fun charge(activity: Activity)
}

interface UnlockAppListener {
    fun onLockStatusChanged(status: Boolean)

    fun showFailToConnectFeedback()
}
