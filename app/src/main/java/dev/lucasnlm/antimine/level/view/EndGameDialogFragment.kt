package dev.lucasnlm.antimine.level.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.instant.InstantAppManageable
import dev.lucasnlm.antimine.instant.InstantAppManager
import dev.lucasnlm.antimine.level.viewmodel.EngGameDialogViewModel
import dev.lucasnlm.antimine.share.viewmodel.ShareViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EndGameDialogFragment : AppCompatDialogFragment() {

    @Inject
    lateinit var viewModelFactory: GameViewModelFactory

    @Inject
    lateinit var instantAppManager: InstantAppManageable

    private lateinit var endGameViewModel: EngGameDialogViewModel
    private lateinit var viewModel: GameViewModel
    private lateinit var shareViewModel: ShareViewModel

    private var hasValidData = false
    private var isVictory: Boolean = false
    private var time: Long = 0L
    private var rightMines: Int = 0
    private var totalMines: Int = 0
    private var saveId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it, viewModelFactory).get(GameViewModel::class.java)
            endGameViewModel = ViewModelProviders.of(this).get(EngGameDialogViewModel::class.java)
            shareViewModel = ViewModelProviders.of(this).get(ShareViewModel::class.java)
        }

        arguments?.run {
            isVictory = getBoolean(DIALOG_IS_VICTORY) == true
            time = getLong(DIALOG_TIME, 0L)
            rightMines = getInt(DIALOG_RIGHT_MINES, 0)
            totalMines = getInt(DIALOG_TOTAL_MINES, 0)
            hasValidData = (totalMines > 0)
            saveId = getLong(DIALOG_SAVE_ID, 0L)
        }
    }

    fun showAllowingStateLoss(manager: FragmentManager, tag: String?) {
        val fragmentTransaction = manager.beginTransaction()
        fragmentTransaction.add(this, tag)
        fragmentTransaction.commitAllowingStateLoss()
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(context!!, R.style.MyDialog).apply {
            val view = LayoutInflater
                .from(context)
                .inflate(R.layout.dialog_end_game, null, false)
                .apply {
                    val titleEmoji: String
                    val title: String
                    val message: String

                    when {
                        !hasValidData -> {
                            titleEmoji = endGameViewModel.randomNeutralEmoji()
                            title = context.getString(R.string.new_game)
                            message = context.getString(R.string.new_game_request)
                        }
                        isVictory -> {
                            titleEmoji = endGameViewModel.randomVictoryEmoji()
                            title = context.getString(R.string.you_won)
                            message = endGameViewModel.messageTo(context, rightMines, totalMines, time, isVictory)
                        }
                        else -> {
                            titleEmoji = endGameViewModel.randomGameOverEmoji()
                            title = context.getString(R.string.you_lost)
                            message = endGameViewModel.messageTo(context, rightMines, totalMines, time, isVictory)
                        }
                    }

                    findViewById<TextView>(R.id.title).text = title
                    findViewById<TextView>(R.id.subtitle).text = message
                    findViewById<TextView>(R.id.title_emoji).apply {
                        text = titleEmoji
                        setOnClickListener {
                            text = when {
                                !hasValidData -> endGameViewModel.randomNeutralEmoji(text.toString())
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
            } else if (isVictory) {
                setNeutralButton(R.string.share) { _, _ ->
                    val setup = viewModel.levelSetup.value
                    val field = viewModel.field.value

                    GlobalScope.launch {
                        shareViewModel.share(setup, field, time)
                    }
                }
            } else {
                setNeutralButton(R.string.retry) { _, _ ->
                    GlobalScope.launch {
                        viewModel.retryGame(saveId.toInt())
                    }
                }
            }
        }.create()

    companion object {
        fun newInstance(victory: Boolean, rightMines: Int, totalMines: Int, time: Long, saveId: Long) =
            EndGameDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(DIALOG_IS_VICTORY, victory)
                    putInt(DIALOG_RIGHT_MINES, rightMines)
                    putInt(DIALOG_TOTAL_MINES, totalMines)
                    putLong(DIALOG_TIME, time)
                    putLong(DIALOG_SAVE_ID, saveId)
                }
            }

        const val DIALOG_IS_VICTORY = "dialog_state"
        private const val DIALOG_TIME = "dialog_time"
        private const val DIALOG_RIGHT_MINES = "dialog_right_mines"
        private const val DIALOG_TOTAL_MINES = "dialog_total_mines"
        private const val DIALOG_SAVE_ID = "dialog_save_id"

        val TAG = EndGameDialogFragment::class.simpleName
    }
}
