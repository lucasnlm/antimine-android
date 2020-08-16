package dev.lucasnlm.external

import android.app.Activity

interface IBillingManager {
    fun start()
    suspend fun charge(activity: Activity)
}
