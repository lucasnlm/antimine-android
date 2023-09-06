package dev.lucasnlm.external

import android.app.Activity
import dev.lucasnlm.external.model.Price
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.flow.Flow

interface BillingManager {
    fun start()

    fun isEnabled(): Boolean

    suspend fun charge(activity: Activity)

    suspend fun getPrice(): Price?

    suspend fun getPriceFlow(): Flow<Price>

    fun listenPurchases(): Flow<PurchaseInfo>
}
