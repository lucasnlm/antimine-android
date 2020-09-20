package dev.lucasnlm.antimine.support

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.analytics.IAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.models.Analytics
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.external.Ads
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.InstantAppManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SupportAppDialogFragment : AppCompatDialogFragment() {
    private val billingManager: IBillingManager by inject()
    private val analyticsManager: IAnalyticsManager by inject()
    private val preferenceRepository: IPreferencesRepository by inject()
    private val adsManager: IAdsManager by inject()
    private val instantAppManager: InstantAppManager by inject()

    private var unlockMessage: Int = R.string.support_action
    private var targetThemeId: Long = -1L

    private var isInstantMode: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isInstantMode = instantAppManager.isEnabled(requireContext())

        billingManager.start()
        analyticsManager.sentEvent(Analytics.ShowIapDialog)

        unlockMessage =
            (arguments?.getInt(UNLOCK_LABEL) ?: savedInstanceState?.getInt(UNLOCK_LABEL)) ?: R.string.support_action
        targetThemeId =
            (arguments?.getLong(TARGET_THEME_ID, -1L)) ?: -1L
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setView(R.layout.dialog_payments)

            if (isInstantMode) {
                setNeutralButton(R.string.no) { _, _ ->
                    analyticsManager.sentEvent(Analytics.DenyIapDialog)
                }

                val unlockMessage = context.getString(R.string.try_it)
                setNeutralButton("$unlockMessage \uD83C\uDF9E️") { _, _ ->
                    activity?.let {
                        if (!it.isFinishing) {
                            adsManager.requestRewarded(
                                it,
                                Ads.RewardsAds,
                                onRewarded = {
                                    preferenceRepository.useTheme(targetThemeId)
                                    it.recreate()
                                },
                                onFail = {
                                    Toast.makeText(it.applicationContext, R.string.unknown_error, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            )
                        }
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
                                        it.recreate()
                                    },
                                    onFail = {
                                        Toast.makeText(it.applicationContext, R.string.unknown_error, Toast.LENGTH_SHORT)
                                            .show()
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

        fun newRequestSupportDialog(): SupportAppDialogFragment {
            return SupportAppDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(UNLOCK_LABEL, R.string.support_action)
                }
            }
        }

        fun newRemoveAdsSupportDialog(): SupportAppDialogFragment {
            return SupportAppDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(UNLOCK_LABEL, R.string.remove_ad)
                }
            }
        }

        fun newChangeThemeDialog(targetThemeId: Long): SupportAppDialogFragment {
            return SupportAppDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(UNLOCK_LABEL, R.string.unlock_all)
                    putLong(TARGET_THEME_ID, targetThemeId)
                }
            }
        }
    }
}
