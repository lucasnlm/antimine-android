package dev.lucasnlm.antimine.level.view

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders

import dagger.android.support.DaggerAppCompatDialogFragment
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.data.DifficultyPreset
import dev.lucasnlm.antimine.common.level.data.LevelSetup
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModelFactory
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class CustomLevelDialogFragment : DaggerAppCompatDialogFragment() {
    @Inject
    lateinit var viewModelFactory: GameViewModelFactory

    @Inject
    lateinit var preferencesRepository: IPreferencesRepository

    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it, viewModelFactory).get(GameViewModel::class.java)
        }
    }

    private fun filterInput(target: String, min: Int): Int {
        var result = min

        try {
            result = Integer.valueOf(target)
        } catch (e: NumberFormatException) {
            result = min
        } finally {
            result = result.coerceAtLeast(min)
        }

        return result
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!, R.style.MyDialog).apply {
            setTitle(R.string.new_game)
            setView(R.layout.dialog_custom_game)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.start) { _, _ ->
                val mapWidth: TextView? = dialog?.findViewById(R.id.map_width)
                val mapHeight: TextView? = dialog?.findViewById(R.id.map_height)
                val mapMines: TextView? = dialog?.findViewById(R.id.map_mines)

                var width = filterInput(mapWidth?.text.toString(), MIN_WIDTH)
                var height = filterInput(mapHeight?.text.toString(), MIN_HEIGHT)
                var mines = filterInput(mapMines?.text.toString(), MIN_MINES)

                if (width * height - 1 < mines) {
                    mines = width * height - 1
                }

                width = width.coerceAtMost(50)
                height = height.coerceAtMost(50)
                mines = mines.coerceAtLeast(1)

                preferencesRepository.updateCustomGameMode(LevelSetup(width, height, mines))

                GlobalScope.launch(Dispatchers.IO) {
                    viewModel.startNewGame(DifficultyPreset.Custom)
                }
            }
        }.create()
    }

    companion object {
        const val MIN_WIDTH = 5
        const val MIN_HEIGHT = 5
        const val MIN_MINES = 3

        val TAG = CustomLevelDialogFragment::class.simpleName
    }
}
