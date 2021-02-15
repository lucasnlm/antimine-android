package dev.lucasnlm.antimine.gameover

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.isAndroidTv
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.gameover.model.GameResult
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogEvent
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogViewModel
import dev.lucasnlm.antimine.level.view.NewGameFragment
import dev.lucasnlm.antimine.preferences.PreferencesActivity
import dev.lucasnlm.antimine.stats.StatsActivity
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.IFeatureFlagManager
import dev.lucasnlm.external.IInstantAppManager
import dev.lucasnlm.external.ReviewWrapper
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
    private val reviewWrapper: ReviewWrapper by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!preferencesRepository.isPremiumEnabled()) {
            billingManager.start()
        }

        arguments?.run {
            endGameViewModel.sendEvent(
                EndGameDialogEvent.BuildCustomEndGame(
                    gameResult = if (getInt(DIALOG_TOTAL_MINES, 0) > 0) {
                        GameResult.values()[getInt(DIALOG_GAME_RESULT)]
                    } else GameResult.GameOver,
                    showContinueButton = getBoolean(DIALOG_SHOW_CONTINUE),
                    time = getLong(DIALOG_TIME, 0L),
                    rightMines = getInt(DIALOG_RIGHT_MINES, 0),
                    totalMines = getInt(DIALOG_TOTAL_MINES, 0),
                    received = getInt(DIALOG_RECEIVED, -1)
                )
            )
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        activity?.let {
            if (!it.isFinishing) {
                reviewWrapper.startInAppReview(it)
            }
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
            val view = LayoutInflater
                .from(context)
                .inflate(R.layout.win_dialog, null, false)
                .apply {
                    lifecycleScope.launchWhenCreated {
                        endGameViewModel.observeState().collect { state ->
                            val shareButton: View = findViewById(R.id.share)
                            val statsButton: AppCompatButton = findViewById(R.id.stats)
                            val newGameButton: AppCompatButton = findViewById(R.id.new_game)
                            val removeAdsButton: AppCompatButton = findViewById(R.id.remove_ads)
                            val settingsButton: View = findViewById(R.id.settings)
                            val receivedMessage: TextView = findViewById(R.id.received_message)
                            val title: TextView = findViewById(R.id.title)
                            val subtitle: TextView = findViewById(R.id.subtitle)
                            val emoji: ImageView = findViewById(R.id.title_emoji)

                            title.text = state.title
                            subtitle.text = state.message

                            emoji.apply {
                                analyticsManager.sentEvent(Analytics.ClickEmoji)
                                setImageResource(state.titleEmoji)
                                setOnClickListener {
                                    endGameViewModel.sendEvent(
                                        EndGameDialogEvent.ChangeEmoji(state.gameResult, state.titleEmoji)
                                    )
                                }
                            }

                            shareButton.setOnClickListener {
                                analyticsManager.sentEvent(Analytics.ShareGame)
                                gameViewModel.shareObserver.postValue(Unit)
                            }

                            statsButton.setOnClickListener {
                                analyticsManager.sentEvent(Analytics.OpenStats)
                                Intent(context, StatsActivity::class.java).apply {
                                    startActivity(this)
                                }
                            }

                            newGameButton.setOnClickListener {
                                if (featureFlagManager.isAdsOnContinueEnabled &&
                                    !preferencesRepository.isPremiumEnabled()
                                ) {
                                    showAdsAndNewGame()
                                } else {
                                    if (context.isAndroidTv()) {
                                        NewGameFragment().show(parentFragmentManager, NewGameFragment.TAG)
                                    } else {
                                        gameViewModel.startNewGame()
                                    }
                                    dismissAllowingStateLoss()
                                }
                            }

                            if (!preferencesRepository.isPremiumEnabled() &&
                                featureFlagManager.isAdsOnContinueEnabled
                            ) {
                                newGameButton.compoundDrawablePadding = 0
                                newGameButton.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.watch_ads_icon, 0, 0, 0
                                )
                            }

                            settingsButton.setOnClickListener {
                                analyticsManager.sentEvent(Analytics.OpenSettings)
                                showSettings()
                            }

                            if (state.gameResult == GameResult.Victory || state.gameResult == GameResult.Completed) {
                                if (!instantAppManager.isEnabled(context)) {
                                    shareButton.visibility = View.GONE
                                }
                                statsButton.visibility = View.VISIBLE
                                shareButton.visibility = View.VISIBLE
                            }

                            if (!preferencesRepository.isPremiumEnabled() &&
                                !instantAppManager.isEnabled(context) &&
                                featureFlagManager.isGameOverAdEnabled
                            ) {
                                activity?.let { activity ->
                                    val label = context.getString(R.string.remove_ad)
                                    val price = billingManager.getPrice()
                                    val unlockLabel = price?.let { "$label - $it" } ?: label
                                    removeAdsButton.apply {
                                        analyticsManager.sentEvent(Analytics.RemoveAds)
                                        visibility = View.VISIBLE
                                        text = unlockLabel
                                        setOnClickListener {
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
                                    preferencesRepository.useHelp()
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

            setView(view)
        }.create().apply {
            setCanceledOnTouchOutside(false)
        }

    private fun showSettings() {
        startActivity(Intent(requireContext(), PreferencesActivity::class.java))
    }

    private fun startNewGameAndDismiss() {
        activity?.let { activity ->
            if (activity.isAndroidTv()) {
                NewGameFragment().show(parentFragmentManager, NewGameFragment.TAG)
            } else {
                gameViewModel.startNewGame()
            }
            dismissAllowingStateLoss()
        }
    }

    private fun showAdsAndNewGame() {
        activity?.let { activity ->
            if (!activity.isFinishing) {
                adsManager.requestRewardedAd(
                    activity,
                    onRewarded = {
                        startNewGameAndDismiss()
                    },
                    onFail = {
                        startNewGameAndDismiss()
                    }
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
