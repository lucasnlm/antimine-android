package dev.lucasnlm.antimine.support

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.R
import dev.lucasnlm.external.IBillingManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SupportAppDialogFragment : AppCompatDialogFragment() {
    private val billingManager: IBillingManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        billingManager.start()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setView(R.layout.dialog_payments)
            setNeutralButton(R.string.rating_button_no, null)
            setPositiveButton(R.string.unlock) { _, _ ->
                lifecycleScope.launch {
                    billingManager.charge(requireActivity())
                }
            }
        }.create()
    }

    companion object {
        val TAG = SupportAppDialogFragment::class.simpleName
    }
}
