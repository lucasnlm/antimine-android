package dev.lucasnlm.antimine.gameover

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.databinding.GameOverDialogBinding
import dev.lucasnlm.antimine.gameover.model.GameResult
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogEvent
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogViewModel
import dev.lucasnlm.antimine.themes.ThemeActivity
import dev.lucasnlm.antimine.tutorial.TutorialActivity
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.FeatureFlagManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameOverDialogFragment : CommonGameDialogFragment() {
    private val analyticsManager: AnalyticsManager by inject()
    private val dialogViewModel by viewModel<EndGameDialogViewModel>()
    private val gameViewModel by sharedViewModel<GameViewModel>()
    private val featureFlagManager: FeatureFlagManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.run {
            dialogViewModel.sendEvent(
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
                    turn = getInt(DIALOG_TURN, 0),
                ),
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        return MaterialAlertDialogBuilder(context).apply {
            val layoutInflater = LayoutInflater.from(context)
            val binding = GameOverDialogBinding.inflate(layoutInflater, null, false)

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

                        newGame.setOnClickListener {
                            lifecycleScope.launch {
                                gameViewModel.startNewGame()
                            }
                            dismissAllowingStateLoss()
                        }

                        continueGame.setOnClickListener {
                            analyticsManager.sentEvent(Analytics.ContinueGame)
                            if (featureFlagManager.isAdsOnContinueEnabled && !isPremiumEnabled) {
                                showAdsAndContinue()
                            } else {
                                gameViewModel.sendEvent(GameEvent.ContinueGame)
                                dismissAllowingStateLoss()
                            }
                        }

                        settings.setOnClickListener {
                            analyticsManager.sentEvent(Analytics.OpenSettings)
                            showSettings()
                        }

                        close.setOnClickListener {
                            analyticsManager.sentEvent(Analytics.CloseEndGameScreen)
                            activity?.let {
                                if (!it.isFinishing) {
                                    lifecycleScope.launch {
                                        gameViewModel.revealMines()
                                    }
                                }
                            }
                            dismissAllowingStateLoss()
                        }

                        if (featureFlagManager.isFoss && canRequestDonation) {
                            showDonationDialog(adFrame)
                        } else if (!isPremiumEnabled && featureFlagManager.isBannerAdEnabled) {
                            showAdBannerDialog(adFrame)
                        }

                        if (!state.showTutorial &&
                            state.showContinueButton &&
                            featureFlagManager.isContinueGameEnabled
                        ) {
                            continueGame.isVisible = true
                            if (!isPremiumEnabled && featureFlagManager.isAdsOnContinueEnabled) {
                                continueGame.compoundDrawablePadding = 0
                                continueGame.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.watch_ads_icon,
                                    0,
                                    0,
                                    0,
                                )
                            }

                            if (!isPremiumEnabled && featureFlagManager.showCountdownToContinue) {
                                countdown.isVisible = true
                                lifecycleScope.launch {
                                    var countdownTime = 10
                                    while (countdownTime > 0) {
                                        countdown.text = countdownTime.toString()
                                        delay(1000L)
                                        countdownTime -= 1
                                    }
                                    countdown.isVisible = false
                                    continueGame.isVisible = false
                                }
                            }
                        } else {
                            continueGame.isVisible = false
                            countdown.isVisible = false
                        }

                        if (state.showTutorial) {
                            tutorial.isVisible = true
                            tutorial.setOnClickListener {
                                val intent = Intent(context, TutorialActivity::class.java)
                                context.startActivity(intent)
                            }
                        } else if (
                            !isPremiumEnabled &&
                            !isInstantMode &&
                            featureFlagManager.isGameOverAdEnabled
                        ) {
                            activity?.let { activity ->
                                val label = context.getString(R.string.remove_ad)
                                val priceModel = billingManager.getPrice()
                                val price = priceModel?.price
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
                        } else if (!isPremiumEnabled && isInstantMode) {
                            removeAds.apply {
                                isVisible = true
                                text = getString(R.string.themes)
                                setOnClickListener {
                                    val intent = Intent(context, ThemeActivity::class.java)
                                    startActivity(intent)
                                }
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

    override fun continueGame() {
        gameViewModel.sendEvent(GameEvent.ContinueGame)
        dismissAllowingStateLoss()
    }

    override fun canShowMusicBanner(): Boolean {
        return dialogViewModel.singleState().showMusicDialog
    }

    companion object {
        fun newInstance(
            gameResult: GameResult,
            showContinueButton: Boolean,
            rightMines: Int,
            totalMines: Int,
            time: Long,
            received: Int,
            turn: Int,
        ) = GameOverDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(DIALOG_GAME_RESULT, gameResult.ordinal)
                putBoolean(DIALOG_SHOW_CONTINUE, showContinueButton)
                putInt(DIALOG_RIGHT_MINES, rightMines)
                putInt(DIALOG_TOTAL_MINES, totalMines)
                putInt(DIALOG_RECEIVED, received)
                putLong(DIALOG_TIME, time)
                putInt(DIALOG_TURN, turn)
            }
        }

        private const val DIALOG_GAME_RESULT = "dialog_game_result"
        private const val DIALOG_SHOW_CONTINUE = "dialog_show_continue"
        private const val DIALOG_TIME = "dialog_time"
        private const val DIALOG_RIGHT_MINES = "dialog_right_mines"
        private const val DIALOG_TOTAL_MINES = "dialog_total_mines"
        private const val DIALOG_RECEIVED = "dialog_received"
        private const val DIALOG_TURN = "dialog_turn"
    }
}
