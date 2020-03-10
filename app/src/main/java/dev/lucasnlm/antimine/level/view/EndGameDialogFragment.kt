package dev.lucasnlm.antimine.level.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerAppCompatDialogFragment
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.instant.InstantAppManager
import dev.lucasnlm.antimine.level.viewmodel.EngGameDialogViewModel
import dev.lucasnlm.antimine.share.viewmodel.ShareViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class EndGameDialogFragment : DaggerAppCompatDialogFragment() {

    @Inject
    lateinit var viewModelFactory: GameViewModelFactory

    @Inject
    lateinit var instantAppManager: InstantAppManager

    private lateinit var endGameViewModel: EngGameDialogViewModel
    private lateinit var viewModel: GameViewModel
    private lateinit var shareViewModel: ShareViewModel

    private var isVictory: Boolean = false
    private var time: Long = 0L
    private var rightMines: Int = 0
    private var totalMines: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it, viewModelFactory).get(GameViewModel::class.java)
            endGameViewModel = ViewModelProviders.of(this).get(EngGameDialogViewModel::class.java)
            shareViewModel = ViewModelProviders.of(this).get(ShareViewModel::class.java)
        }

        isVictory = arguments?.getBoolean(DIALOG_STATE) == true
        time = arguments?.getLong(DIALOG_TIME) ?: 0L
        rightMines = arguments?.getInt(DIALOG_RIGHT_MINES) ?: 0
        totalMines = arguments?.getInt(DIALOG_TOTAL_MINES) ?: 0
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(context!!, R.style.MyDialog).apply {
            val view = LayoutInflater
                .from(context)
                .inflate(R.layout.dialog_end_game, null, false)
                .apply {
                    val title = when {
                        isVictory -> endGameViewModel.randomVictoryEmoji()
                        else -> endGameViewModel.randomGameOverEmoji()
                    }

                    val titleRes =
                        if (isVictory) R.string.you_won else R.string.you_lost
                    val message = endGameViewModel.messageTo(context, rightMines, totalMines, time, isVictory)

                    findViewById<TextView>(R.id.title).text = context.getString(titleRes)
                    findViewById<TextView>(R.id.subtitle).text = message
                    findViewById<TextView>(R.id.title_emoji).apply {
                        text = title
                        setOnClickListener {
                            text = when {
                                isVictory -> endGameViewModel.randomVictoryEmoji(text.toString())
                                else -> endGameViewModel.randomGameOverEmoji(text.toString())
                            }
                        }
                    }
                }

            setView(view)

            setPositiveButton(R.string.new_game) { _, _ ->
                GlobalScope.launch {
                    viewModel.startNewGame()
                }
            }

            if (instantAppManager.isEnabled()) {
                setNeutralButton(R.string.install) { _, _ ->
                    activity?.run {
                        instantAppManager.showInstallPrompt(this, null, 0, null)
                    }
                }
            } else {
                setNeutralButton(R.string.share) { _, _ ->
                    val setup = viewModel.levelSetup.value
                    val field = viewModel.field.value

                    GlobalScope.launch {
                        shareViewModel.share(setup, field, time)
                    }
                }
            }
        }.create()

    companion object {
        fun newInstance(victory: Boolean, rightMines: Int, totalMines: Int, time: Long): EndGameDialogFragment =
            EndGameDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(DIALOG_STATE, victory)
                    putInt(DIALOG_RIGHT_MINES, rightMines)
                    putInt(DIALOG_TOTAL_MINES, totalMines)
                    putLong(DIALOG_TIME, time)
                }
            }

        private const val DIALOG_STATE = "dialog_state"
        private const val DIALOG_TIME = "dialog_time"
        private const val DIALOG_RIGHT_MINES = "dialog_right_mines"
        private const val DIALOG_TOTAL_MINES = "dialog_total_mines"

        val TAG = EndGameDialogFragment::class.simpleName
    }
}
