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
import dev.lucasnlm.external.model.Price
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import java.util.*
import kotlin.math.roundToInt

class BillingManager(
    private val context: Context,
    private val crashReporter: CrashReporter,
    private val featureFlagManager: FeatureFlagManager,
) : IBillingManager, BillingClientStateListener, PurchasesUpdatedListener {
    private var retry = 0
    private val purchaseBroadcaster = ConflatedBroadcastChannel<PurchaseInfo>()
    private val unlockPrice = MutableStateFlow<Price?>(null)
    private val billingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }

    private val giveOffer: Boolean by lazy {
        if (featureFlagManager.isWeekDaySalesEnabled) {
            val calendar = Calendar.getInstance()
            when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.THURSDAY, Calendar.TUESDAY, Calendar.WEDNESDAY -> true
                else -> false
            }
        } else {
            false
        }
    }

    override suspend fun getPrice(): Price? = unlockPrice.value

    override suspend fun getPriceFlow(): Flow<Price> {
        return unlockPrice.asSharedFlow().filterNotNull()
    }

    override fun listenPurchases(): Flow<PurchaseInfo> = purchaseBroadcaster.asFlow()

    private fun handlePurchases(purchases: List<Purchase>) {
        val status: Boolean = purchases.firstOrNull {
            it.sku == PREMIUM || it.sku == PREMIUM50
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
                .setSkusList(listOf(PREMIUM, PREMIUM50))
                .setType(BillingClient.SkuType.INAPP)
                .build()

            billingClient
                .querySkuDetailsAsync(skuDetailsParams) { _, list ->
                    val fullPrice = list?.firstOrNull {
                        it.sku == PREMIUM
                    }

                    val halfSize = list?.firstOrNull {
                        it.sku == PREMIUM50
                    }

                    if (fullPrice != null && halfSize != null) {
                        val percent = halfSize.priceAmountMicros.toFloat() / fullPrice.priceAmountMicros.toFloat()
                        val percentInt = 5 * ((percent * 100.0f / 5f).roundToInt())
                        val percentText = "$percentInt\nOFF"

                        val price = if (giveOffer) {
                            Price(
                                halfSize.price,
                                percentText,
                            )
                        } else {
                            Price(
                                fullPrice.price,
                                null,
                            )
                        }

                        unlockPrice.tryEmit(price)
                    }
                }

            val purchasesList: List<Purchase> = billingClient
                .queryPurchases(BillingClient.SkuType.INAPP)
                .purchasesList.let { it?.toList() ?: listOf() }

            handlePurchases(purchasesList)
        } else {
            val code = billingResult.responseCode
            val message = billingResult.debugMessage
            crashReporter.sendError("Billing setup failed due to response $code. $message")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        val purchasesList: List<Purchase> = if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // The BillingClient is ready. You can query purchases here.
            purchases?.toList() ?: listOf()
        } else {
            crashReporter.sendError("Charge update failed due to response ${billingResult.responseCode}")
            listOf()
        }

        handlePurchases(purchasesList)
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
            val item = if (giveOffer) {
                listOf(PREMIUM50)
            } else {
                listOf(PREMIUM)
            }

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
            purchaseBroadcaster.offer(PurchaseInfo.PurchaseFail)
        }
    }

    companion object {
        private const val PREMIUM = "unlock_0"
        private const val PREMIUM50 = "unlock_1"
    }
}
