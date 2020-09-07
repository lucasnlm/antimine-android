package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.querySkuDetails
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class BillingManager(
    private val context: Context,
) : IBillingManager, BillingClientStateListener, PurchasesUpdatedListener {

    private val purchaseBroadcaster = ConflatedBroadcastChannel<PurchaseInfo>()

    private val billingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }

    override fun listenPurchases(): Flow<PurchaseInfo> = purchaseBroadcaster.asFlow()

    private fun handlePurchases(purchases: List<Purchase>) {
        val status: Boolean = purchases.firstOrNull {
            it.sku == BASIC_SUPPORT
        }.let {
            when (it?.purchaseState) {
                Purchase.PurchaseState.PURCHASED, Purchase.PurchaseState.PENDING -> true
                else -> false
            }.also { purchased ->
                if (purchased && it?.isAcknowledged == false) {
                    val acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(it.purchaseToken)
                            .build()

                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) {
                        // Purchase acknowledged
                    }
                }
            }
        }

        purchaseBroadcaster.offer(PurchaseInfo.PurchaseResult(isFreeUnlock = false, unlockStatus = status))
    }

    override fun onBillingServiceDisconnected() {
        // Try to restart the connection on the next request to
        // Google Play by calling the startConnection() method.
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val purchasesList: List<Purchase> = if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // The BillingClient is ready. You can query purchases here.

            billingClient.queryPurchases(BillingClient.SkuType.INAPP).purchasesList.let {
                it?.toList() ?: listOf()
            }
        } else {
            listOf()
        }

        handlePurchases(purchasesList)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        val purchasesList: List<Purchase> = if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // The BillingClient is ready. You can query purchases here.
            purchases?.toList() ?: listOf()
        } else {
            listOf()
        }

        handlePurchases(purchasesList)
    }

    override fun start() {
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
            purchaseBroadcaster.offer(PurchaseInfo.PurchaseFail)
        }
    }

    companion object {
        private const val BASIC_SUPPORT = "unlock_0"
    }
}
