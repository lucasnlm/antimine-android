package dev.lucasnlm.antimine.support

import android.content.Context
import android.widget.Toast
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.external.BillingManager
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import dev.lucasnlm.antimine.i18n.R as i18n

class IapHandler(
    private val context: Context,
    private val preferencesManager: PreferencesRepository,
    private val billingManager: BillingManager,
) {
    private val billingListener = MutableStateFlow(false)

    suspend fun start() {
        billingManager.listenPurchases().collect {
            if (it is PurchaseInfo.PurchaseResult) {
                onLockStatusChanged(it.unlockStatus)
            } else {
                showFailToConnectFeedback()
            }
        }
    }

    fun isEnabled() = billingManager.isEnabled()

    fun listenPurchase(): Flow<Boolean> = billingListener.asStateFlow()

    private fun onLockStatusChanged(status: Boolean) {
        billingListener.tryEmit(status)
        preferencesManager.setPremiumFeatures(status)
    }

    private suspend fun showFailToConnectFeedback() {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, i18n.string.sign_in_failed, Toast.LENGTH_SHORT).show()
        }
    }
}
