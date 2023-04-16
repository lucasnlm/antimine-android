package dev.lucasnlm.antimine.gameover

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.databinding.WinDialogBinding
import dev.lucasnlm.antimine.gameover.model.GameResult
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogEvent
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogViewModel
import dev.lucasnlm.antimine.stats.StatsActivity
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IFeatureFlagManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class WinGameDialogFragment : CommonGameDialogFragment() {
    private val analyticsManager: IAnalyticsManager by inject()
    private val dialogViewmodel by viewModel<EndGameDialogViewModel>()
    private val gameViewModel by sharedViewModel<GameViewModel>()
    private val featureFlagManager: IFeatureFlagManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.run {
            dialogViewmodel.sendEvent(
                EndGameDialogEvent.BuildCustomEndGame(
                    gameResult = if (getInt(DIALOG_TOTAL_MINES, 0) > 0) {
                        GameResult.values()[getInt(DIALOG_GAME_RESULT)]
                    } else {
                        GameResult.GameOver
                    },
                    showContinueButton = getBoolean(DIALOG_SHOW_CONTINUE),
                    time = getLong(DIALOG_TIME, 0L),
                    rightMines = getInt(DIALOG_RIGHT_MINES, 0),
                    totalMines = getInt(DIALOG_TOTAL_MINES, 0),
                    received = getInt(DIALOG_RECEIVED, -1),
                    turn = -1,
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
        return dialogViewmodel.singleState().showMusicDialog
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        return MaterialAlertDialogBuilder(context).apply {
            val layoutInflater = LayoutInflater.from(context)
            val binding = WinDialogBinding.inflate(layoutInflater, null, false)

            binding.run {
                lifecycleScope.launch {
                    dialogViewmodel.observeState().collect { state ->
                        title.text = state.title
                        subtitle.text = state.message

                        titleEmoji.apply {
                            setImageResource(state.titleEmoji)
                            setOnClickListener {
                                analyticsManager.sentEvent(Analytics.ClickEmoji)
                                dialogViewmodel.sendEvent(
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
                            if (featureFlagManager.isAdsOnContinueEnabled && !isPremiumEnabled) {
                                showAdsAndContinue()
                            } else {
                                continueGame()
                            }
                        }

                        if (!isPremiumEnabled && featureFlagManager.isAdsOnContinueEnabled
                        ) {
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

                        if (!isPremiumEnabled &&
                            !isInstantMode &&
                            featureFlagManager.isGameOverAdEnabled
                        ) {
                            activity?.let { activity ->
                                val label = context.getString(R.string.remove_ad)
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
                                text = getString(R.string.you_have_received, state.received)
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    attributes?.blurBehindRadius = 8
                }
            }
        }
    }

    companion object {
        fun newInstance(
            gameResult: GameResult,
            showContinueButton: Boolean,
            rightMines: Int,
            totalMines: Int,
            time: Long,
            received: Int,
        ) = WinGameDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(DIALOG_GAME_RESULT, gameResult.ordinal)
                putBoolean(DIALOG_SHOW_CONTINUE, showContinueButton)
                putInt(DIALOG_RIGHT_MINES, rightMines)
                putInt(DIALOG_TOTAL_MINES, totalMines)
                putInt(DIALOG_RECEIVED, received)
                putLong(DIALOG_TIME, time)
            }
        }

        const val DIALOG_GAME_RESULT = "dialog_game_result"
        private const val DIALOG_SHOW_CONTINUE = "dialog_show_continue"
        private const val DIALOG_TIME = "dialog_time"
        private const val DIALOG_RIGHT_MINES = "dialog_right_mines"
        private const val DIALOG_TOTAL_MINES = "dialog_total_mines"
        private const val DIALOG_RECEIVED = "dialog_received"

        val TAG = WinGameDialogFragment::class.simpleName!!
    }
}
