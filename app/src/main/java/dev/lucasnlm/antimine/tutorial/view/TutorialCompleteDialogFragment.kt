package dev.lucasnlm.antimine.tutorial.view

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
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class TutorialCompleteDialogFragment : AppCompatDialogFragment() {
    private val gameViewModel by sharedViewModel<GameViewModel>()

    fun showAllowingStateLoss(manager: FragmentManager, tag: String?) {
        val fragmentTransaction = manager.beginTransaction()
        fragmentTransaction.add(this, tag)
        fragmentTransaction.commitAllowingStateLoss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameViewModel.startNewGame(Difficulty.Beginner)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext()).apply {
            val view = LayoutInflater
                .from(context)
                .inflate(R.layout.dialog_end_game, null, false)
                .apply {
                    findViewById<TextView>(R.id.title).text = getString(R.string.tutorial_completed)
                    findViewById<TextView>(R.id.subtitle).visibility = View.GONE
                    findViewById<ImageView>(R.id.title_emoji)
                        .setImageResource(R.drawable.emoji_beaming_face_with_smiling_eyes)
                }

            setView(view)
            setNeutralButton(R.string.retry) { _, _ ->
                gameViewModel.eventObserver.postValue(Event.StartTutorial)
            }
            setPositiveButton(R.string.resume) { _, _ -> }
        }.create()

    companion object {
        val TAG = TutorialCompleteDialogFragment::class.simpleName!!
    }
}
