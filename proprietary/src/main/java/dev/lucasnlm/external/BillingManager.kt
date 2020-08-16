package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.querySkuDetails

class BillingManager(
    private val context: Context
) : IBillingManager {
    private val purchaseUpdateListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                // The BillingClient is ready. You can query purchases here.
            }
        }

    private val billingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()
    }

    override fun start() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    override suspend fun charge(activity: Activity) {
        val skuDetailsParams = SkuDetailsParams.newBuilder()
            .setSkusList(listOf(BASIC_SUPPORT))
            .setType(BillingClient.SkuType.INAPP)
            .build()

        val details = billingClient.querySkuDetails(skuDetailsParams)

print(details.toString())

        //billingClient.launchBillingFlow(activity, flowParams)
    }

    companion object {
        private const val BASIC_SUPPORT = "unlock_0"
    }
}
