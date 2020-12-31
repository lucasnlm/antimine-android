package dev.lucasnlm.antimine.support

import android.content.Context
import android.widget.Toast
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

class IapHandler(
    private val context: Context,
    private val preferencesManager: IPreferencesRepository,
    private val billingManager: IBillingManager,
) {
    suspend fun start() {
        billingManager.listenPurchases().collect {
            if (it is PurchaseInfo.PurchaseResult) {
                onLockStatusChanged(it.unlockStatus)
            } else {
                showFailToConnectFeedback()
            }
        }
    }

    private fun onLockStatusChanged(status: Boolean) {
        preferencesManager.setPremiumFeatures(status)
    }

    private suspend fun showFailToConnectFeedback() {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, R.string.sign_in_failed, Toast.LENGTH_SHORT).show()
        }
    }
}
