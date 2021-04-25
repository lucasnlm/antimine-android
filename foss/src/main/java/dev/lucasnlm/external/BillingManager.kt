package dev.lucasnlm.external

import android.app.Activity
import android.content.Intent
import android.net.Uri
import dev.lucasnlm.external.model.Price
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class BillingManager : IBillingManager {
    override fun start() {
        // Empty
    }

    override fun isEnabled(): Boolean = false

    override suspend fun charge(activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DONATE_LINK))
        activity.startActivity(intent)
    }

    override suspend fun getPrice(): Price? = null

    override suspend fun getPriceFlow(): Flow<Price> = flowOf()

    override fun listenPurchases(): Flow<PurchaseInfo> = flowOf()

    companion object {
        const val DONATE_LINK =
            "https://www.paypal.com/donate?hosted_button_id=49XX9XDNUV4SW"
    }
}
