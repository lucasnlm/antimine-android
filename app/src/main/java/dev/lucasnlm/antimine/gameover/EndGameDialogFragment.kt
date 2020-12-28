package dev.lucasnlm.antimine.gameover

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.gameover.model.GameResult
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogEvent
import dev.lucasnlm.antimine.gameover.viewmodel.EndGameDialogViewModel
import dev.lucasnlm.antimine.preferences.PreferencesActivity
import dev.lucasnlm.external.Ads
import dev.lucasnlm.external.IAdsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.IInstantAppManager
import dev.lucasnlm.external.view.AdPlaceHolderView
import kotlinx.android.synthetic.main.view_play_games_button.view.*
import kotlinx.android.synthetic.main.view_stats.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class EndGameDialogFragment : AppCompatDialogFragment() {
    private val adsManager: IAdsManager by inject()
    private val instantAppManager: IInstantAppManager by inject()
    private val endGameViewModel by viewModel<EndGameDialogViewModel>()
    private val gameViewModel by sharedViewModel<GameViewModel>()
    private val preferencesRepository: IPreferencesRepository by inject()
    private val billingManager: IBillingManager by inject()
    private var revealMinesOnDismiss = true

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

    fun showAllowingStateLoss(manager: FragmentManager, tag: String?) {
        val fragmentTransaction = manager.beginTransaction()
        fragmentTransaction.add(this, tag)
        fragmentTransaction.commitAllowingStateLoss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (revealMinesOnDismiss) {
            activity?.let {
                if (!it.isFinishing) {
                    gameViewModel.viewModelScope.launch {
                        gameViewModel.revealMines()
                    }
                }
            }
        }
        super.onDismiss(dialog)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext()).apply {
            val view = LayoutInflater
                .from(context)
                .inflate(R.layout.dialog_end_game, null, false)
                .apply {
                    lifecycleScope.launchWhenCreated {
                        endGameViewModel.observeState().collect { state ->
                            findViewById<TextView>(R.id.title).text = state.title
                            findViewById<TextView>(R.id.subtitle).text = state.message
                            findViewById<ImageView>(R.id.title_emoji).apply {
                                setImageResource(state.titleEmoji)
                                setOnClickListener {
                                    endGameViewModel.sendEvent(
                                        EndGameDialogEvent.ChangeEmoji(state.gameResult, state.titleEmoji)
                                    )
                                }
                            }

                            val adsView: AdPlaceHolderView = findViewById(R.id.ads)
                            val shareButton: AppCompatButton = findViewById(R.id.share)
                            val newGameButton: AppCompatButton = findViewById(R.id.new_game)
                            val continueButton: AppCompatButton = findViewById(R.id.continue_game)
                            val removeAdsButton: AppCompatButton = findViewById(R.id.remove_ads)
                            val settingsButton: View = findViewById(R.id.settings)
                            val closeButton: View = findViewById(R.id.close)
                            val receivedMessage: TextView = findViewById(R.id.received_message)

                            shareButton.setOnClickListener {
                                revealMinesOnDismiss = false
                                gameViewModel.shareObserver.postValue(Unit)
                            }

                            newGameButton.setOnClickListener {
                                revealMinesOnDismiss = false
                                gameViewModel.startNewGame()
                                dismissAllowingStateLoss()
                            }

                            continueButton.setOnClickListener {
                                revealMinesOnDismiss = false

                                if (preferencesRepository.isPremiumEnabled()) {
                                    gameViewModel.continueObserver.postValue(Unit)
                                    dismissAllowingStateLoss()
                                } else {
                                    showAdsAndContinue()
                                }
                            }

                            settingsButton.setOnClickListener {
                                showSettings()
                            }

                            closeButton.setOnClickListener {
                                dismissAllowingStateLoss()
                            }

                            if (state.gameResult == GameResult.Victory) {
                                if (!instantAppManager.isEnabled(context)) {
                                    shareButton.visibility = View.GONE
                                }
                                shareButton.visibility = View.VISIBLE
                                continueButton.visibility = View.GONE
                            } else {
                                shareButton.visibility = View.GONE

                                if (state.showContinueButton) {
                                    continueButton.visibility = View.VISIBLE
                                    if (!preferencesRepository.isPremiumEnabled()) {
                                        continueButton.compoundDrawablePadding = 0
                                        continueButton.setCompoundDrawablesWithIntrinsicBounds(
                                            R.drawable.watch_ads_icon, 0, 0, 0
                                        )
                                    }
                                } else {
                                    continueButton.visibility = View.GONE
                                }
                            }

                            if (!preferencesRepository.isPremiumEnabled() && !instantAppManager.isEnabled(context)) {
                                activity?.let { activity ->
                                    adsView.visibility = View.VISIBLE
                                    removeAdsButton.visibility = View.VISIBLE
                                    val label = context.getString(R.string.remove_ad)
                                    val price = billingManager.getPrice().singleOrNull()
                                    val unlockLabel = price?.let { "$label - $it" } ?: label
                                    removeAdsButton.text = unlockLabel
                                    removeAdsButton.setOnClickListener {
                                        lifecycleScope.launch {
                                            billingManager.charge(activity)
                                            adsView.visibility = View.GONE
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

            setView(view)
        }.create()

    private fun showSettings() {
        startActivity(Intent(requireContext(), PreferencesActivity::class.java))
    }

    private fun showAdsAndContinue() {
        activity?.let {
            if (!it.isFinishing) {
                adsManager.requestRewarded(
                    it,
                    Ads.RewardsAds,
                    onRewarded = {
                        gameViewModel.continueObserver.postValue(Unit)
                        dismissAllowingStateLoss()
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

    companion object {
        fun newInstance(
            gameResult: GameResult,
            showContinueButton: Boolean,
            rightMines: Int,
            totalMines: Int,
            time: Long,
            received: Int,
        ) = EndGameDialogFragment().apply {
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

        val TAG = EndGameDialogFragment::class.simpleName!!
    }
}
