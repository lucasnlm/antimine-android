package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.querySkuDetails

class BillingManager(
    private val context: Context,
) : IBillingManager, BillingClientStateListener, PurchasesUpdatedListener {
    private var unlockAppListener: UnlockAppListener? = null

    private val billingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        purchases.firstOrNull {
            it.sku == BASIC_SUPPORT
        }?.let {
            if (it.purchaseState == Purchase.PurchaseState.PURCHASED) {
                unlockAppListener?.onLockStatusChanged(isFreeUnlock = false, status = true)
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        // Try to restart the connection on the next request to
        // Google Play by calling the startConnection() method.
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // The BillingClient is ready. You can query purchases here.

            billingClient.queryPurchases(BillingClient.SkuType.INAPP).purchasesList?.let {
                handlePurchases(it.toList())
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // The BillingClient is ready. You can query purchases here.
            purchases?.let {
                handlePurchases(it.toList())
            }
        } else {
            unlockAppListener?.onLockStatusChanged(status = false)
        }
    }

    override fun start(unlockAppListener: UnlockAppListener) {
        this.unlockAppListener = unlockAppListener
        billingClient.startConnection(this)
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override suspend fun charge(activity: Activity) {
        val skuDetailsParams = SkuDetailsParams.newBuilder()
            .setSkusList(listOf(BASIC_SUPPORT))
            .setType(BillingClient.SkuType.INAPP)
            .build()

        if (billingClient.isReady) {
            val details = billingClient.querySkuDetails(skuDetailsParams)
            details.skuDetailsList?.firstOrNull()?.let {
                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(it)
                    .build()

                billingClient.launchBillingFlow(activity, flowParams)
            }
        } else {
            unlockAppListener?.showFailToConnectFeedback()
        }
    }

    companion object {
        private const val BASIC_SUPPORT = "unlock_0"
    }
}
