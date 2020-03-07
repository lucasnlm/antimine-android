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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class EndGameDialogFragment : DaggerAppCompatDialogFragment() {
    private val gameOverEmojis = listOf(
        "\uD83D\uDE10","\uD83D\uDE44", "\uD83D\uDE25", "\uD83D\uDE13", "\uD83D\uDE31",
        "\uD83E\uDD2C", "\uD83E\uDD15", "\uD83D\uDE16", "\uD83D\uDCA3", "\uD83D\uDE05")

    private val victoryEmojis = listOf(
        "\uD83D\uDE00", "\uD83D\uDE0E", "\uD83D\uDE1D", "\uD83E\uDD73", "\uD83D\uDE06")


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
        return AlertDialog.Builder(context!!, R.style.MyDialog).apply {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_end_game, null, false)

            val emojis = if (isVictory) victoryEmojis else gameOverEmojis
            val titleRes = if (isVictory) R.string.you_won else R.string.you_lost

            view.findViewById<TextView>(R.id.title_emoji).text = emojis.random()
            view.findViewById<TextView>(R.id.title).text = context.getString(titleRes)
            view.findViewById<TextView>(R.id.subtitle).text = arguments?.getString(DIALOG_MESSAGE)
            setView(view)

            setPositiveButton(R.string.new_game) { _, _ ->
                GlobalScope.launch {
                    viewModel.startNewGame()
                }
            }
            setNeutralButton(R.string.share) { _, _ ->}

        }.create()
    }

    companion object {
        fun newInstance(message: String, victory: Boolean): EndGameDialogFragment =
            EndGameDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(DIALOG_STATE, victory)
                    putString(DIALOG_MESSAGE, message)}
            }

        private const val DIALOG_STATE = "dialog_state"
        private const val DIALOG_MESSAGE = "dialog_message"
    }
}
