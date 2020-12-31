package dev.lucasnlm.antimine.purchases

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.external.Ads
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.IInstantAppManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SupportAppDialogFragment : AppCompatDialogFragment() {
    private val billingManager: IBillingManager by inject()
    private val analyticsManager: IAnalyticsManager by inject()
    private val preferenceRepository: IPreferencesRepository by inject()
    private val adsManager: IAdsManager by inject()
    private val instantAppManager: IInstantAppManager by inject()

    private lateinit var unlockMessage: String
    private var targetThemeId: Long = -1L

    private var isInstantMode: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isInstantMode = instantAppManager.isEnabled(requireContext())

        billingManager.start()
        analyticsManager.sentEvent(Analytics.ShowIapDialog)

        unlockMessage =
            (arguments?.getString(UNLOCK_LABEL) ?: savedInstanceState?.getString(UNLOCK_LABEL))
            ?: getString(R.string.support_action)
        targetThemeId =
            (arguments?.getLong(TARGET_THEME_ID, -1L)) ?: -1L
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            val view = LayoutInflater
                .from(context)
                .inflate(R.layout.dialog_payments, null, false)
                .apply {
                    findViewById<View>(R.id.close).setOnClickListener {
                        dismissAllowingStateLoss()
                    }
                }

            setView(view)

            if (isInstantMode) {
                val unlockMessage = context.getString(R.string.try_it)
                setNeutralButton("$unlockMessage \uD83C\uDF9E️") { _, _ ->
                    activity?.let {
                        if (!it.isFinishing) {
                            adsManager.requestRewarded(
                                it,
                                Ads.RewardsAds,
                                onRewarded = {
                                    preferenceRepository.useTheme(targetThemeId)
                                    recreateActivity()
                                },
                                onFail = {
                                    Toast.makeText(it.applicationContext, R.string.unknown_error, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            )
                        }
                    }
                }

                setPositiveButton(R.string.unlock_all) { _, _ ->
                    lifecycleScope.launch {
                        preferenceRepository.setShowSupport(false)
                        analyticsManager.sentEvent(Analytics.UnlockIapDialog)
                        billingManager.charge(requireActivity())
                    }
                }
            } else {
                if (targetThemeId != -1L) {
                    val unlockMessage = context.getString(R.string.try_it)
                    setNeutralButton("$unlockMessage \uD83C\uDF9E️") { _, _ ->
                        activity?.let {
                            if (!it.isFinishing) {
                                adsManager.requestRewarded(
                                    it,
                                    Ads.RewardsAds,
                                    onRewarded = {
                                        preferenceRepository.useTheme(targetThemeId)
                                        recreateActivity()
                                    },
                                    onFail = {
                                        Toast.makeText(
                                            it.applicationContext,
                                            R.string.unknown_error,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    }
                } else {
                    setNeutralButton(R.string.no) { _, _ ->
                        analyticsManager.sentEvent(Analytics.DenyIapDialog)
                    }
                }

                setPositiveButton(unlockMessage) { _, _ ->
                    lifecycleScope.launch {
                        preferenceRepository.setShowSupport(false)
                        analyticsManager.sentEvent(Analytics.UnlockIapDialog)
                        billingManager.charge(requireActivity())
                    }
                }
            }
        }.create()
    }

    private fun recreateActivity() {
        activity?.let {
            it.finish()
            it.startActivity(it.intent)
            it.overridePendingTransition(0, 0)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (activity is DialogInterface.OnDismissListener) {
            (activity as DialogInterface.OnDismissListener).onDismiss(dialog)
        }
        super.onDismiss(dialog)
    }

    companion object {
        val TAG = SupportAppDialogFragment::class.simpleName

        private const val UNLOCK_LABEL = "support_unlock_label"
        private const val TARGET_THEME_ID = "target_theme_id"

        fun newRequestSupportDialog(context: Context): SupportAppDialogFragment {
            return SupportAppDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(UNLOCK_LABEL, context.getString(R.string.support_action))
                }
            }
        }

        fun newRemoveAdsSupportDialog(context: Context, price: String?): SupportAppDialogFragment {
            return SupportAppDialogFragment().apply {
                val label = context.getString(R.string.remove_ad)
                val unlockLabel = price?.let { "$label - $it" } ?: label
                arguments = Bundle().apply {
                    putString(UNLOCK_LABEL, unlockLabel)
                }
            }
        }

        fun newChangeThemeDialog(context: Context, targetThemeId: Long, price: String?): SupportAppDialogFragment {
            return SupportAppDialogFragment().apply {
                val label = context.getString(R.string.unlock_all)
                val unlockLabel = price?.let { "$label - $it" } ?: label
                arguments = Bundle().apply {
                    putString(UNLOCK_LABEL, unlockLabel)
                    putLong(TARGET_THEME_ID, targetThemeId)
                }
            }
        }
    }
}
