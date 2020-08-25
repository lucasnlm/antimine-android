package dev.lucasnlm.antimine.support

import android.content.Context
import android.widget.Toast
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.external.UnlockAppListener

class IapHandler(
    private val context: Context,
    private val preferencesManager: IPreferencesRepository,
) : UnlockAppListener {
    override fun onLockStatusChanged(isFreeUnlock: Boolean, status: Boolean) {
        preferencesManager.setPremiumFeatures(status, isFreeUnlock)
    }

    override fun showFailToConnectFeedback() {
        Toast.makeText(context, R.string.sign_in_failed, Toast.LENGTH_SHORT).show()
    }
}
