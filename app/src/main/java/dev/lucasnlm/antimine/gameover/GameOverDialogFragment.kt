package dev.lucasnlm.antimine.gameover

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.format.DateUtils
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
import dev.lucasnlm.antimine.gameover.model.CommonDialogState
import dev.lucasnlm.antimine.gameover.model.GameResult
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogEvent
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogViewModel
import dev.lucasnlm.antimine.themes.ThemeActivity
import dev.lucasnlm.antimine.tutorial.TutorialActivity
import dev.lucasnlm.antimine.utils.BuildExt.androidSnowCone
import dev.lucasnlm.antimine.utils.BundleExt.parcelable
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.FeatureFlagManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import dev.lucasnlm.antimine.i18n.R as i18n

class GameOverDialogFragment : CommonGameDialogFragment() {
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
                            if (!isPremiumEnabled) {
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

                        if (!state.showTutorial && state.showContinueButton) {
                            continueGame.isVisible = true
                            if (!isPremiumEnabled) {
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
                                    repeat(CONTINUE_COUNTDOWN_SECONDS) {
                                        countdown.text = (CONTINUE_COUNTDOWN_SECONDS - it).toString()
                                        delay(DateUtils.SECOND_IN_MILLIS)
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
                        } else if (!isPremiumEnabled && !isInstantMode) {
                            activity?.let { activity ->
                                val label = context.getString(i18n.string.remove_ad)
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
                                text = getString(i18n.string.themes)
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
                androidSnowCone {
                    addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    attributes?.blurBehindRadius = BACKGROUND_BLUR_RADIUS
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
        fun newInstance(state: CommonDialogState) =
            GameOverDialogFragment().apply {
                arguments =
                    Bundle().apply {
                        putParcelable(DIALOG_STATE, state)
                    }
            }

        private const val CONTINUE_COUNTDOWN_SECONDS = 10
    }
}
