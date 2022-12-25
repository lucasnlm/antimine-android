package dev.lucasnlm.antimine.gameover

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.gameover.model.GameResult
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogEvent
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogViewModel
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.PreferencesActivity
import dev.lucasnlm.antimine.themes.ThemeActivity
import dev.lucasnlm.antimine.tutorial.TutorialActivity
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.IInstantAppManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameOverDialogFragment : AppCompatDialogFragment() {
    private val analyticsManager: IAnalyticsManager by inject()
    private val adsManager: IAdsManager by inject()
    private val instantAppManager: IInstantAppManager by inject()
    private val endGameViewModel by viewModel<EndGameDialogViewModel>()
    private val gameViewModel by sharedViewModel<GameViewModel>()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val billingManager: IBillingManager by inject()
    private val featureFlagManager: IFeatureFlagManager by inject()
    private val themeRepository: IThemeRepository by inject()

    private lateinit var usingTheme: AppTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        usingTheme = themeRepository.getTheme()

        if (!preferencesRepository.isPremiumEnabled()) {
            billingManager.start()
        }

        arguments?.run {
            endGameViewModel.sendEvent(
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

    fun showAllowingStateLoss(manager: FragmentManager, tag: String?) {
        val fragmentTransaction = manager.beginTransaction()
        fragmentTransaction.add(this, tag)
        fragmentTransaction.commitAllowingStateLoss()
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        return MaterialAlertDialogBuilder(context).apply {
            val view = LayoutInflater
                .from(context)
                .inflate(R.layout.game_over_dialog, null, false)
                .apply {
                    lifecycleScope.launchWhenCreated {
                        endGameViewModel.observeState().collect { state ->
                            val newGameButton: AppCompatButton = findViewById(R.id.new_game)
                            val continueButton: AppCompatButton = findViewById(R.id.continue_game)
                            val countdown: TextView = findViewById(R.id.countdown)
                            val removeAdsButton: AppCompatButton = findViewById(R.id.remove_ads)
                            val tutorialButton: AppCompatButton = findViewById(R.id.tutorial)
                            val settingsButton: View = findViewById(R.id.settings)
                            val closeButton: View = findViewById(R.id.close)
                            val title: TextView = findViewById(R.id.title)
                            val subtitle: TextView = findViewById(R.id.subtitle)
                            val emoji: ImageView = findViewById(R.id.title_emoji)
                            val adFrame: FrameLayout = findViewById(R.id.adFrame)

                            title.text = state.title
                            subtitle.text = state.message

                            emoji.apply {
                                setImageResource(state.titleEmoji)
                                setOnClickListener {
                                    analyticsManager.sentEvent(Analytics.ClickEmoji)
                                    endGameViewModel.sendEvent(
                                        EndGameDialogEvent.ChangeEmoji(state.gameResult, state.titleEmoji),
                                    )
                                }
                            }

                            newGameButton.setOnClickListener {
                                lifecycleScope.launch {
                                    gameViewModel.startNewGame()
                                }
                                dismissAllowingStateLoss()
                            }

                            continueButton.setOnClickListener {
                                analyticsManager.sentEvent(Analytics.ContinueGame)
                                if (featureFlagManager.isAdsOnContinueEnabled &&
                                    !preferencesRepository.isPremiumEnabled()
                                ) {
                                    showAdsAndContinue()
                                } else {
                                    gameViewModel.sendEvent(GameEvent.ContinueGame)
                                    dismissAllowingStateLoss()
                                }
                            }

                            settingsButton.setOnClickListener {
                                analyticsManager.sentEvent(Analytics.OpenSettings)
                                showSettings()
                            }

                            closeButton.setOnClickListener {
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

                            if (featureFlagManager.isFoss && preferencesRepository.requestDonation()) {
                                adFrame.visibility = View.VISIBLE

                                val view = View.inflate(context, R.layout.donation_request, null)
                                view.setOnClickListener {
                                    activity?.let {
                                        lifecycleScope.launch {
                                            billingManager.charge(it)
                                            preferencesRepository.setRequestDonation(false)
                                        }
                                    }
                                }

                                adFrame.addView(
                                    view,
                                    FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.WRAP_CONTENT,
                                        Gravity.CENTER_HORIZONTAL,
                                    ),
                                )
                            } else if (!preferencesRepository.isPremiumEnabled() &&
                                featureFlagManager.isBannerAdEnabled
                            ) {
                                adFrame.visibility = View.VISIBLE

                                adFrame.addView(
                                    adsManager.createBannerAd(context),
                                    FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.WRAP_CONTENT,
                                        Gravity.CENTER_HORIZONTAL,
                                    ),
                                )
                            }

                            if (
                                !state.showTutorial &&
                                state.showContinueButton &&
                                featureFlagManager.isContinueGameEnabled
                            ) {
                                continueButton.visibility = View.VISIBLE
                                if (!preferencesRepository.isPremiumEnabled() &&
                                    featureFlagManager.isAdsOnContinueEnabled
                                ) {
                                    continueButton.compoundDrawablePadding = 0
                                    continueButton.setCompoundDrawablesWithIntrinsicBounds(
                                        R.drawable.watch_ads_icon,
                                        0,
                                        0,
                                        0,
                                    )
                                }

                                if (!preferencesRepository.isPremiumEnabled() &&
                                    featureFlagManager.showCountdownToContinue
                                ) {
                                    countdown.visibility = View.VISIBLE
                                    lifecycleScope.launchWhenCreated {
                                        var countdownTime = 10
                                        while (countdownTime > 0) {
                                            countdown.text = countdownTime.toString()
                                            delay(1000L)
                                            countdownTime -= 1
                                        }
                                        countdown.visibility = View.GONE
                                        continueButton.visibility = View.GONE
                                    }
                                }
                            } else {
                                continueButton.visibility = View.GONE
                                countdown.visibility = View.GONE
                            }

                            if (state.showTutorial) {
                                tutorialButton.visibility = View.VISIBLE
                                tutorialButton.setOnClickListener {
                                    val intent = Intent(context, TutorialActivity::class.java)
                                    context.startActivity(intent)
                                }
                            } else if (
                                !preferencesRepository.isPremiumEnabled() &&
                                !instantAppManager.isEnabled(context) &&
                                featureFlagManager.isGameOverAdEnabled
                            ) {
                                activity?.let { activity ->
                                    val label = context.getString(R.string.remove_ad)
                                    val priceModel = billingManager.getPrice()
                                    val price = priceModel?.price
                                    val unlockLabel = price?.let { "$label - $it" } ?: label
                                    removeAdsButton.apply {
                                        visibility = View.VISIBLE
                                        text = unlockLabel

                                        setOnClickListener {
                                            analyticsManager.sentEvent(Analytics.RemoveAds)
                                            lifecycleScope.launch {
                                                billingManager.charge(activity)
                                            }
                                        }
                                    }
                                }
                            } else if (!preferencesRepository.isPremiumEnabled() &&
                                instantAppManager.isEnabled(context)
                            ) {
                                removeAdsButton.apply {
                                    visibility = View.VISIBLE
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

            setView(view)
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

    private fun showSettings() {
        startActivity(Intent(requireContext(), PreferencesActivity::class.java))
    }

    private fun continueGame() {
        gameViewModel.sendEvent(GameEvent.ContinueGame)
        dismissAllowingStateLoss()
    }

    private fun showAdsAndContinue() {
        activity?.let { activity ->
            if (!activity.isFinishing) {
                adsManager.showRewardedAd(
                    activity,
                    skipIfFrequent = false,
                    onRewarded = {
                        continueGame()
                    },
                    onFail = {
                        adsManager.showInterstitialAd(
                            activity,
                            onDismiss = {
                                continueGame()
                            },
                            onError = {
                                Toast.makeText(context, R.string.no_network, Toast.LENGTH_SHORT).show()
                            },
                        )
                    },
                )
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
