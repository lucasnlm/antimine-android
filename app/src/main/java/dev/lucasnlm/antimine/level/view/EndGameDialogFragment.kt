package dev.lucasnlm.antimine.level.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.level.viewmodel.EndGameDialogEvent
import dev.lucasnlm.antimine.level.viewmodel.EndGameDialogViewModel
import dev.lucasnlm.external.IInstantAppManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class EndGameDialogFragment : AppCompatDialogFragment() {
    private val instantAppManager: IInstantAppManager by inject()
    private val endGameViewModel by viewModel<EndGameDialogViewModel>()
    private val gameViewModel by sharedViewModel<GameViewModel>()
    private val preferencesRepository: IPreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.run {
            endGameViewModel.sendEvent(
                EndGameDialogEvent.BuildCustomEndGame(
                    isVictory = if (getInt(DIALOG_TOTAL_MINES, 0) > 0) getBoolean(DIALOG_IS_VICTORY) else null,
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
                                        EndGameDialogEvent.ChangeEmoji(state.isVictory, state.titleEmoji)
                                    )
                                }
                            }

                            findViewById<TextView>(R.id.received_message).apply {
                                if (state.received > 0 && state.isVictory == true && preferencesRepository.useHelp()) {
                                    visibility = View.VISIBLE
                                    text = getString(R.string.you_have_received, state.received)
                                } else {
                                    visibility = View.GONE
                                }
                            }

                            findViewById<View>(R.id.close).setOnClickListener {
                                gameViewModel.viewModelScope.launch {
                                    gameViewModel.revealMines()
                                }
                                dismissAllowingStateLoss()
                            }

                            if (state.isVictory == true) {
                                if (!instantAppManager.isEnabled(context)) {
                                    setNeutralButton(R.string.share) { _, _ ->
                                        gameViewModel.shareObserver.postValue(Unit)
                                    }
                                }
                            } else {
                                if (state.showContinueButton) {
                                    setNegativeButton(R.string.retry) { _, _ ->
                                        gameViewModel.retryObserver.postValue(Unit)
                                    }

                                    setNeutralButton(R.string.continue_game) { _, _ ->
                                        gameViewModel.continueObserver.postValue(Unit)
                                    }
                                } else {
                                    setNeutralButton(R.string.retry) { _, _ ->
                                        gameViewModel.retryObserver.postValue(Unit)
                                    }
                                }
                            }
                        }
                    }
                }

            setView(view)

            setPositiveButton(R.string.new_game) { _, _ ->
                gameViewModel.startNewGame()
            }
        }.create()

    companion object {
        fun newInstance(
            victory: Boolean,
            showContinueButton: Boolean,
            rightMines: Int,
            totalMines: Int,
            time: Long,
            received: Int,
        ) = EndGameDialogFragment().apply {
            arguments = Bundle().apply {
                putBoolean(DIALOG_IS_VICTORY, victory)
                putBoolean(DIALOG_SHOW_CONTINUE, showContinueButton)
                putInt(DIALOG_RIGHT_MINES, rightMines)
                putInt(DIALOG_TOTAL_MINES, totalMines)
                putInt(DIALOG_RECEIVED, received)
                putLong(DIALOG_TIME, time)
            }
        }

        const val DIALOG_IS_VICTORY = "dialog_state"
        private const val DIALOG_SHOW_CONTINUE = "dialog_show_continue"
        private const val DIALOG_TIME = "dialog_time"
        private const val DIALOG_RIGHT_MINES = "dialog_right_mines"
        private const val DIALOG_TOTAL_MINES = "dialog_total_mines"
        private const val DIALOG_RECEIVED = "dialog_received"

        val TAG = EndGameDialogFragment::class.simpleName!!
    }
}
