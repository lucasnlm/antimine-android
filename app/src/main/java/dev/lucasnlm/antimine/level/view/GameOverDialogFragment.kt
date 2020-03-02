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

class GameOverDialogFragment : DaggerAppCompatDialogFragment() {
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
        return AlertDialog.Builder(context!!, R.style.MyDialog).apply {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_lose_game, null)
            view.findViewById<TextView>(R.id.title).text = context.getString(R.string.game_over)
            view.findViewById<TextView>(R.id.subtitle).text = "huehue"
            setView(view)

            setPositiveButton("New Game") { _, _ ->
                GlobalScope.launch {
                    viewModel.startNewGame()
                }
            }
            setNeutralButton("Share") { _, _ ->}

        }.create()
    }
}
