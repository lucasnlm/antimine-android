package dev.lucasnlm.external

import android.app.Activity
import android.content.Context

class BillingManager(
    private val context: Context
) : IBillingManager {

    override fun start(unlockAppListener: UnlockAppListener) {
        // Empty
    }

    override suspend fun charge(activity: Activity) {
        // Void
    }
}
