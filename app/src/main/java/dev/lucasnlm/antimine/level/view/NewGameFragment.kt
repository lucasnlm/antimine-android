package dev.lucasnlm.antimine.level.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Difficulty
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class NewGameFragment : AppCompatDialogFragment() {
    private val gameViewModel by sharedViewModel<GameViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            val view: View = LayoutInflater
                .from(context)
                .inflate(R.layout.fragment_new_game, null, false)
                .apply {
                    findViewById<View>(R.id.beginner).setOnClickListener {
                        gameViewModel.startNewGame(Difficulty.Beginner)
                        dismissAllowingStateLoss()
                    }
                    findViewById<View>(R.id.intermediate).setOnClickListener {
                        gameViewModel.startNewGame(Difficulty.Intermediate)
                        dismissAllowingStateLoss()
                    }
                    findViewById<View>(R.id.expert).setOnClickListener {
                        gameViewModel.startNewGame(Difficulty.Expert)
                        dismissAllowingStateLoss()
                    }
                }
            setView(view)
            setCancelable(false)
        }.create().apply {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }
    }

    companion object {
        val TAG = NewGameFragment::class.simpleName
    }
}
