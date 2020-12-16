package dev.lucasnlm.external

import android.app.Activity
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.flow.Flow

interface IBillingManager {
    fun start()
    fun isEnabled(): Boolean
    suspend fun charge(activity: Activity)
    fun getPrice(): Flow<String>
    fun listenPurchases(): Flow<PurchaseInfo>
}
