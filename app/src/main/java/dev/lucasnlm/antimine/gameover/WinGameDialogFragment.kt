package dev.lucasnlm.antimine.gameover

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.databinding.WinDialogBinding
import dev.lucasnlm.antimine.gameover.model.CommonDialogState
import dev.lucasnlm.antimine.gameover.model.GameResult
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogEvent
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogViewModel
import dev.lucasnlm.antimine.stats.StatsActivity
import dev.lucasnlm.antimine.utils.BuildExt.androidSnowCone
import dev.lucasnlm.antimine.utils.BundleExt.parcelable
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.FeatureFlagManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import dev.lucasnlm.antimine.i18n.R as i18n

class WinGameDialogFragment : CommonGameDialogFragment() {
    private val analyticsManager: AnalyticsManager by inject()
    private val dialogViewModel by viewModel<EndGameDialogViewModel>()
    private val gameViewModel by sharedViewModel<GameViewModel>()
    private val featureFlagManager: FeatureFlagManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments
            ?.parcelable<CommonDialogState>(DIALOG_STATE)
            ?.run {
                dialogViewModel.sendEvent(
                    EndGameDialogEvent.BuildCustomEndGame(
                        gameResult =
                            if (totalMines > 0) {
                                gameResult
                            } else {
                                GameResult.GameOver
                            },
                        showContinueButton = showContinueButton,
                        time = time,
                        rightMines = rightMines,
                        totalMines = totalMines,
                        received = received,
                        turn = turn,
                    ),
                )
            }
    }

    override fun continueGame() {
        activity?.let { _ ->
            lifecycleScope.launch {
                gameViewModel.startNewGame()
            }
            dismissAllowingStateLoss()
        }
    }

    override fun canShowMusicBanner(): Boolean {
        return dialogViewModel.singleState().showMusicDialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        return MaterialAlertDialogBuilder(context).apply {
            val layoutInflater = LayoutInflater.from(context)
            val binding = WinDialogBinding.inflate(layoutInflater, null, false)

            binding.run {
                lifecycleScope.launch {
                    dialogViewModel.observeState().collect { state ->
                        title.text = state.title
                        subtitle.text = state.message

                        titleEmoji.apply {
                            setImageResource(state.titleEmoji)
                            setOnClickListener {
                                analyticsManager.sentEvent(Analytics.ClickEmoji)
                                dialogViewModel.sendEvent(
                                    EndGameDialogEvent.ChangeEmoji(state.gameResult, state.titleEmoji),
                                )
                            }
                        }

                        stats.setOnClickListener {
                            analyticsManager.sentEvent(Analytics.OpenStats)
                            Intent(context, StatsActivity::class.java).apply {
                                startActivity(this)
                            }
                        }

                        newGame.setOnClickListener {
                            if (!isPremiumEnabled) {
                                showAdsAndContinue()
                            } else {
                                continueGame()
                            }
                        }

                        if (!isPremiumEnabled) {
                            newGame.compoundDrawablePadding = 0
                            newGame.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.watch_ads_icon,
                                0,
                                0,
                                0,
                            )
                        }

                        if (featureFlagManager.isFoss && canRequestDonation) {
                            showDonationDialog(adFrame)
                        } else if (!isPremiumEnabled && featureFlagManager.isBannerAdEnabled) {
                            showAdBannerDialog(adFrame)
                        } else if (state.showMusicDialog) {
                            showMusicDialog(adFrame)
                        }

                        settings.setOnClickListener {
                            analyticsManager.sentEvent(Analytics.OpenSettings)
                            showSettings()
                        }

                        if (state.gameResult == GameResult.Victory || state.gameResult == GameResult.Completed) {
                            close.setOnClickListener {
                                dismissAllowingStateLoss()
                            }
                            stats.isVisible = true
                        }

                        if (!isPremiumEnabled && !isInstantMode) {
                            activity?.let { activity ->
                                val label = context.getString(i18n.string.remove_ad)
                                val price = billingManager.getPrice()?.price
                                val unlockLabel = price?.let { "$label - $it" } ?: label
                                removeAds.apply {
                                    isVisible = true
                                    text = unlockLabel
                                    setOnClickListener {
                                        analyticsManager.sentEvent(Analytics.RemoveAds)
                                        lifecycleScope.launch {
                                            billingManager.charge(activity)
                                        }
                                    }
                                }
                            }
                        }

                        receivedMessage.apply {
                            if (state.received > 0 &&
                                state.gameResult == GameResult.Victory &&
                                preferencesRepository.useHelp() &&
                                isPremiumEnabled
                            ) {
                                isVisible = true
                                text = getString(i18n.string.you_have_received, state.received)
                            } else {
                                isVisible = false
                            }
                        }
                    }
                }
            }

            setOnKeyListener { _, _, keyEvent ->
                if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                    activity?.let {
                        if (!it.isFinishing) {
                            gameViewModel.viewModelScope.launch {
                                gameViewModel.revealMines()
                            }
                        }
                    }
                    dismissAllowingStateLoss()
                    true
                } else {
                    false
                }
            }

            setView(binding.root)
        }.create().apply {
            setCanceledOnTouchOutside(false)

            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                androidSnowCone {
                    addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    attributes?.blurBehindRadius = BACKGROUND_BLUR_RADIUS
                }
            }
        }
    }

    companion object {
        fun newInstance(commonDialogState: CommonDialogState) =
            WinGameDialogFragment().apply {
                arguments =
                    Bundle().apply {
                        putParcelable(DIALOG_STATE, commonDialogState)
                    }
            }

        val TAG = WinGameDialogFragment::class.simpleName!!
    }
}
