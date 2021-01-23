package dev.lucasnlm.external

import android.app.Activity
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.flow.Flow

interface IBillingManager {
    fun start()
    fun isEnabled(): Boolean
    suspend fun charge(activity: Activity)
    suspend fun getPrice(): String?
    fun listenPurchases(): Flow<PurchaseInfo>
}
