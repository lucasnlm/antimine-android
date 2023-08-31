package dev.lucasnlm.antimine.playgames

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.playgames.view.PlayGamesAdapter
import dev.lucasnlm.antimine.playgames.viewmodel.PlayGamesEvent
import dev.lucasnlm.antimine.playgames.viewmodel.PlayGamesViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import dev.lucasnlm.antimine.i18n.R as i18n

class PlayGamesDialogFragment : AppCompatDialogFragment() {
    private val playGamesViewModel by viewModel<PlayGamesViewModel>()
    private val adapter by lazy { PlayGamesAdapter(playGamesViewModel) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(i18n.string.google_play_games)
            setAdapter(adapter, null)
            setPositiveButton(i18n.string.ok, null)
        }.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            playGamesViewModel.observeEvent().collect {
                when (it) {
                    is PlayGamesEvent.OpenAchievements -> {
                        activity?.let { activity ->
                            playGamesViewModel.openAchievements(activity)
                        }
                    }
                    is PlayGamesEvent.OpenLeaderboards -> {
                        activity?.let { activity ->
                            playGamesViewModel.openLeaderboards(activity)
                        }
                    }
                }
            }
        }
    }

    companion object {
        val TAG = PlayGamesDialogFragment::class.simpleName
    }
}
