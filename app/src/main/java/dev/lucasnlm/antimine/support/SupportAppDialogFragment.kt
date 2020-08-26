package dev.lucasnlm.antimine.support

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.analytics.IAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.models.Analytics
import dev.lucasnlm.external.IBillingManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SupportAppDialogFragment : AppCompatDialogFragment() {
    private val billingManager: IBillingManager by inject()
    private val analyticsManager: IAnalyticsManager by inject()
    private val iapHandler: IapHandler by inject()

    private var unlockMessage: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        billingManager.start(iapHandler)
        analyticsManager.sentEvent(Analytics.ShowIapDialog)

        unlockMessage = (arguments?.getBoolean(UNLOCK_LABEL) ?: savedInstanceState?.getBoolean(UNLOCK_LABEL)) ?: false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setView(R.layout.dialog_payments)
            setNeutralButton(R.string.rating_button_no) { _, _ ->
                analyticsManager.sentEvent(Analytics.DenyIapDialog)
            }
            setPositiveButton(if (unlockMessage) R.string.unlock else R.string.support_action) { _, _ ->
                lifecycleScope.launch {
                    analyticsManager.sentEvent(Analytics.UnlockIapDialog)
                    billingManager.charge(requireActivity())
                }
            }
        }.create()
    }

    companion object {
        val TAG = SupportAppDialogFragment::class.simpleName

        private const val UNLOCK_LABEL = "support_unlock_label"

        fun newInstance(unlockMessage: Boolean): SupportAppDialogFragment {
            return SupportAppDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(UNLOCK_LABEL, unlockMessage)
                }
            }
        }
    }
}
