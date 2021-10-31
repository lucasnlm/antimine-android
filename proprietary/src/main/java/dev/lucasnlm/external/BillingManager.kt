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
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.querySkuDetails
import dev.lucasnlm.external.model.Price
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class BillingManager(
    private val context: Context,
    private val crashReporter: CrashReporter,
    private val coroutineScope: CoroutineScope,
) : IBillingManager, BillingClientStateListener, PurchasesUpdatedListener {
    private var retry = 0
    private val purchaseBroadcaster = MutableStateFlow<PurchaseInfo?>(null)
    private val unlockPrice = MutableStateFlow<Price?>(null)
    private val billingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }

    override suspend fun getPrice(): Price? = unlockPrice.value

    override suspend fun getPriceFlow(): Flow<Price> {
        return unlockPrice.asSharedFlow().filterNotNull()
    }

    override fun listenPurchases(): Flow<PurchaseInfo> = purchaseBroadcaster.asSharedFlow().filterNotNull()

    private fun asyncRefreshPurchasesList() {
        coroutineScope.launch {
            while (true) {
                val purchasesList: List<Purchase> = billingClient
                    .queryPurchases(BillingClient.SkuType.INAPP)
                    .purchasesList.let { it?.toList() ?: listOf() }

                if (purchasesList.isEmpty()) {
                    break
                } else {
                    val result = handlePurchases(purchasesList)

                    if (result) {
                        break
                    } else {
                        delay(30 * 1000L)
                    }
                }
            }
        }
    }

    private suspend fun handlePurchases(purchases: List<Purchase>): Boolean {
        val status: Boolean = purchases.firstOrNull {
            it.sku == PREMIUM
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

                    val result = billingClient.acknowledgePurchase(acknowledgePurchaseParams)

                    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                        return false
                    }
                }
            }
        }

        purchaseBroadcaster.tryEmit(PurchaseInfo.PurchaseResult(isFreeUnlock = false, unlockStatus = status))
        return true
    }

    override fun onBillingServiceDisconnected() {
        crashReporter.sendError("Billing service disconnected $retry")

        if (retry < 3) {
            start()
            retry++
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            retry = 0

            val skuDetailsParams = SkuDetailsParams.newBuilder()
                .setSkusList(listOf(PREMIUM))
                .setType(BillingClient.SkuType.INAPP)
                .build()

            billingClient
                .querySkuDetailsAsync(skuDetailsParams) { _, list ->
                    val hasPremium = list?.firstOrNull {
                        it.sku == PREMIUM
                    }

                    if (hasPremium != null) {
                        val price = Price(
                            hasPremium.price,
                            false,
                        )

                        unlockPrice.tryEmit(price)
                    }
                }

            asyncRefreshPurchasesList()
        } else {
            val code = billingResult.responseCode
            val message = billingResult.debugMessage
            crashReporter.sendError("Billing setup failed due to response $code. $message")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            asyncRefreshPurchasesList()
        } else {
            crashReporter.sendError("Charge update failed due to response ${billingResult.responseCode}")
        }
    }

    override fun start() {
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override suspend fun charge(activity: Activity) {
        if (billingClient.isReady) {
            val item = listOf(PREMIUM)

            val skuDetailsParams = SkuDetailsParams.newBuilder()
                .setSkusList(item)
                .setType(BillingClient.SkuType.INAPP)
                .build()

            val details = billingClient.querySkuDetails(skuDetailsParams)
            details.skuDetailsList?.firstOrNull()?.let {
                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(it)
                    .build()

                billingClient.launchBillingFlow(activity, flowParams)
            }
        } else {
            crashReporter.sendError("Fail to charge due to unready status")
            purchaseBroadcaster.tryEmit(PurchaseInfo.PurchaseFail)
        }
    }

    companion object {
        private const val PREMIUM = "unlock_0"
    }
}
