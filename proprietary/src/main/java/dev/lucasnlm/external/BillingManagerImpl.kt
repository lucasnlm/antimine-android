package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dev.lucasnlm.external.model.Price
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class BillingManagerImpl(
    private val context: Context,
    private val crashReporter: CrashReporterImpl,
    private val coroutineScope: CoroutineScope,
) : BillingManager, BillingClientStateListener, PurchasesUpdatedListener {
    private var retry = 0
    private var isLoading = false
    private val purchaseBroadcaster = MutableStateFlow<PurchaseInfo?>(null)
    private val premiumInAppPrice = MutableStateFlow<Price?>(null)
    private val billingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }

    private val allowedErrorCodes =
        listOf(
            BillingClient.BillingResponseCode.OK,
            BillingClient.BillingResponseCode.USER_CANCELED,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
        )

    private var premiumProduct: ProductDetails? = null

    override suspend fun getPrice(): Price? = premiumInAppPrice.value

    override suspend fun getPriceFlow(): Flow<Price> {
        return premiumInAppPrice.asSharedFlow().filterNotNull()
    }

    override fun listenPurchases(): Flow<PurchaseInfo> = purchaseBroadcaster.asSharedFlow().filterNotNull()

    private fun asyncRefreshPurchasesList(retry: Boolean) {
        val queryPurchasesParams =
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

        billingClient
            .queryPurchasesAsync(queryPurchasesParams) { result, purchasesList ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    if (retry) {
                        coroutineScope.launch {
                            delay(DateUtils.SECOND_IN_MILLIS * HANDLE_PURCHASES_DELAY_SECONDS)
                            asyncRefreshPurchasesList(false)
                        }
                    }
                } else if (purchasesList.isNotEmpty()) {
                    handlePurchases(purchasesList)
                }
            }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        val status: Boolean =
            purchases.firstOrNull {
                it.products.contains(PREMIUM)
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

                        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { result ->
                            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                                Log.e(TAG, "Fail to acknowledge purchase")
                            }
                        }
                    }
                }
            }

        purchaseBroadcaster.tryEmit(
            PurchaseInfo.PurchaseResult(
                isFreeUnlock = false,
                unlockStatus = status,
            ),
        )
    }

    override fun onBillingServiceDisconnected() {
        crashReporter.sendError("Billing service disconnected $retry")
        isLoading = false

        if (retry < MAX_RETRY_CONNECTION) {
            retry++
            coroutineScope.launch {
                delay(retry * RETRY_SECONDS_STEP_SECONDS * DateUtils.SECOND_IN_MILLIS)
                start()
            }
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        isLoading = false

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            retry = 0

            val premiumProductParams =
                QueryProductDetailsParams
                    .Product
                    .newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .setProductId(PREMIUM)
                    .build()

            val productDetailsParams =
                QueryProductDetailsParams.newBuilder()
                    .setProductList(listOf(premiumProductParams))
                    .build()

            billingClient
                .queryProductDetailsAsync(productDetailsParams) { _, list ->
                    onReceivePremiumProduct(list.firstOrNull())
                }

            asyncRefreshPurchasesList(true)
        } else {
            val code = billingResult.responseCode
            val message = billingResult.debugMessage
            crashReporter.sendError("Billing setup failed due to response $code. $message")
        }
    }

    private fun onReceivePremiumProduct(productDetails: ProductDetails?) {
        val premiumProductDetails = productDetails?.productId == PREMIUM

        if (productDetails != null && premiumProductDetails) {
            premiumProduct = productDetails
            val premiumPrice = productDetails.oneTimePurchaseOfferDetails?.formattedPrice

            if (premiumPrice != null) {
                val price =
                    Price(
                        premiumPrice,
                        offer = false,
                    )

                premiumInAppPrice.tryEmit(price)
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        val resultCode = billingResult.responseCode
        if (resultCode == BillingClient.BillingResponseCode.OK) {
            asyncRefreshPurchasesList(true)
        } else if (!allowedErrorCodes.contains(resultCode)) {
            crashReporter.sendError("Charge update failed due to response $resultCode")
        }
    }

    override fun start() {
        if (!billingClient.isReady && !isLoading) {
            billingClient.startConnection(this)
        }
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override suspend fun charge(activity: Activity) {
        val premiumProduct = this.premiumProduct

        if (billingClient.isReady && premiumProduct != null) {
            val productDetailsParams =
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(premiumProduct)
                    .build()

            val flowParams =
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(productDetailsParams))
                    .build()

            billingClient.launchBillingFlow(activity, flowParams)
        } else {
            crashReporter.sendError("Fail to charge due to unready status")
            purchaseBroadcaster.tryEmit(PurchaseInfo.PurchaseFail)
        }
    }

    companion object {
        private const val TAG = "BillingManager"
        private const val PREMIUM = "unlock_0"
        private const val RETRY_SECONDS_STEP_SECONDS = 5
        private const val MAX_RETRY_CONNECTION = 3
        private const val HANDLE_PURCHASES_DELAY_SECONDS = 10
    }
}
