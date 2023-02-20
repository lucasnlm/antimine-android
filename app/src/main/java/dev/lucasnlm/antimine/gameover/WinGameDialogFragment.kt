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
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
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
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.PreferencesActivity
import dev.lucasnlm.antimine.stats.StatsActivity
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.IInstantAppManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class WinGameDialogFragment : AppCompatDialogFragment() {
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
                    turn = -1,
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
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext()).apply {
            val layoutInflater = LayoutInflater.from(context)
            val binding = WinDialogBinding.inflate(layoutInflater, null, false)

            binding.run {
                lifecycleScope.launchWhenCreated {
                    endGameViewModel.observeState().collect { state ->
                        title.text = state.title
                        subtitle.text = state.message

                        titleEmoji.apply {
                            setImageResource(state.titleEmoji)
                            setOnClickListener {
                                analyticsManager.sentEvent(Analytics.ClickEmoji)
                                endGameViewModel.sendEvent(
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
                            if (featureFlagManager.isAdsOnContinueEnabled &&
                                !preferencesRepository.isPremiumEnabled()
                            ) {
                                showAdsAndNewGame()
                            } else {
                                lifecycleScope.launch {
                                    gameViewModel.startNewGame()
                                }
                                dismissAllowingStateLoss()
                            }
                        }

                        if (!preferencesRepository.isPremiumEnabled() &&
                            featureFlagManager.isAdsOnContinueEnabled
                        ) {
                            newGame.compoundDrawablePadding = 0
                            newGame.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.watch_ads_icon,
                                0,
                                0,
                                0,
                            )
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

                        settings.setOnClickListener {
                            analyticsManager.sentEvent(Analytics.OpenSettings)
                            showSettings()
                        }

                        if (state.gameResult == GameResult.Victory || state.gameResult == GameResult.Completed) {
                            close.setOnClickListener {
                                dismissAllowingStateLoss()
                            }
                            stats.visibility = View.VISIBLE
                        }

                        if (!preferencesRepository.isPremiumEnabled() &&
                            !instantAppManager.isEnabled(context) &&
                            featureFlagManager.isGameOverAdEnabled
                        ) {
                            activity?.let { activity ->
                                val label = context.getString(R.string.remove_ad)
                                val price = billingManager.getPrice()?.price
                                val unlockLabel = price?.let { "$label - $it" } ?: label
                                removeAds.apply {
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
                        }

                        receivedMessage.apply {
                            if (state.received > 0 &&
                                state.gameResult == GameResult.Victory &&
                                preferencesRepository.useHelp() &&
                                preferencesRepository.isPremiumEnabled()
                            ) {
                                visibility = View.VISIBLE
                                text = getString(R.string.you_have_received, state.received)
                            } else {
                                visibility = View.GONE
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

    private fun showSettings() {
        startActivity(Intent(requireContext(), PreferencesActivity::class.java))
    }

    private fun startNewGameAndDismiss() {
        activity?.let { _ ->
            lifecycleScope.launch {
                gameViewModel.startNewGame()
            }
            dismissAllowingStateLoss()
        }
    }

    private fun showAdsAndNewGame() {
        activity?.let { activity ->
            if (!activity.isFinishing) {
                adsManager.showRewardedAd(
                    activity,
                    skipIfFrequent = false,
                    onRewarded = {
                        startNewGameAndDismiss()
                    },
                    onFail = {
                        adsManager.showInterstitialAd(
                            activity,
                            onDismiss = {
                                startNewGameAndDismiss()
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
