package dev.lucasnlm.antimine.tutorial.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.preferences.PreferencesActivity
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
                .inflate(R.layout.dialog_tutorial_completed, null, false)
                .apply {
                    findViewById<TextView>(R.id.title).text = getString(R.string.tutorial_completed)
                    findViewById<ImageView>(R.id.title_emoji)
                        .setImageResource(R.drawable.emoji_beaming_face_with_smiling_eyes)

                    findViewById<View>(R.id.settings).setOnClickListener {
                        showSettings()
                    }

                    findViewById<View>(R.id.continue_game).setOnClickListener {
                        dismissAllowingStateLoss()
                    }

                    findViewById<View>(R.id.close).setOnClickListener {
                        dismissAllowingStateLoss()
                    }
                }
            setView(view)
        }.create()

    private fun showSettings() {
        startActivity(Intent(requireContext(), PreferencesActivity::class.java))
    }

    companion object {
        val TAG = TutorialCompleteDialogFragment::class.simpleName!!
    }
}
