package dev.lucasnlm.antimine.level.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerAppCompatDialogFragment
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.ShareManager
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class EndGameDialogFragment : DaggerAppCompatDialogFragment() {
    private val gameOverEmojis = listOf(
        "\uD83D\uDE10", "\uD83D\uDE44", "\uD83D\uDE25", "\uD83D\uDE13", "\uD83D\uDE31",
        "\uD83E\uDD2C", "\uD83E\uDD15", "\uD83D\uDE16", "\uD83D\uDCA3", "\uD83D\uDE05"
    )

    private val victoryEmojis = listOf(
        "\uD83D\uDE00", "\uD83D\uDE0E", "\uD83D\uDE1D", "\uD83E\uDD73", "\uD83D\uDE06"
    )


    @Inject
    lateinit var viewModelFactory: GameViewModelFactory

    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it, viewModelFactory).get(GameViewModel::class.java)
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val isVictory = arguments?.getBoolean(DIALOG_STATE) == true

        val time: Long = arguments?.getLong(DIALOG_TIME) ?: 0L
        val rightMines: Int = arguments?.getInt(DIALOG_RIGHT_MINES) ?: 0
        val totalMines: Int = arguments?.getInt(DIALOG_TOTAL_MINES) ?: 0

        val message: String = when {
            isVictory ->
                getString(R.string.game_over_desc_4, time)
            rightMines / totalMines > 0.9 ->
                getString(R.string.game_over_desc_3)
            rightMines < 4 ->
                getString(arrayOf(R.string.game_over_desc_0, R.string.game_over_desc_1).random())
            else ->
                getString(R.string.game_over_desc_2, rightMines, totalMines, time)
        }

        return AlertDialog.Builder(context!!, R.style.MyDialog).apply {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_end_game, null, false)

            val emojis = if (isVictory) victoryEmojis else gameOverEmojis
            val titleRes = if (isVictory) R.string.you_won else R.string.you_lost

            view.findViewById<TextView>(R.id.title_emoji).text = emojis.random()
            view.findViewById<TextView>(R.id.title).text = context.getString(titleRes)
            view.findViewById<TextView>(R.id.subtitle).text = message
            setView(view)

            setPositiveButton(R.string.new_game) { _, _ ->
                GlobalScope.launch {
                    viewModel.startNewGame()
                }
            }
            setNeutralButton(R.string.share) { _, _ ->
                val setup = viewModel.levelSetup.value
                val field = viewModel.field.value

                if (setup != null && field != null) {
                    ShareManager(context, setup, field).share(rightMines, time.toInt())
                } else {
                    Toast.makeText(context, context.getString(R.string.fail_to_share), Toast.LENGTH_SHORT).show()
                }
            }

        }.create()
    }

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
    }
}
