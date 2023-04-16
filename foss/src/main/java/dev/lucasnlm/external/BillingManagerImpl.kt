package dev.lucasnlm.external

import android.app.Activity
import android.content.Intent
import android.net.Uri
import dev.lucasnlm.external.model.Price
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class BillingManagerImpl : BillingManager {
    override fun start() {
        // Empty
    }

    override fun isEnabled(): Boolean = false

    override suspend fun charge(activity: Activity) {
        val donationDeeplink = "app://antimine/donation"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(donationDeeplink))
        activity.startActivity(intent)
    }

    override suspend fun getPrice(): Price? = null

    override suspend fun getPriceFlow(): Flow<Price> = flowOf()

    override fun listenPurchases(): Flow<PurchaseInfo> = flowOf()
}
