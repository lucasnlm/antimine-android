package dev.lucasnlm.antimine.custom

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.models.Difficulty
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.custom.viewmodel.CreateGameViewModel
import javax.inject.Inject

@AndroidEntryPoint
class CustomLevelDialogFragment : AppCompatDialogFragment() {
    @Inject
    lateinit var preferencesRepository: IPreferencesRepository

    private val viewModel by activityViewModels<GameViewModel>()
    private val createGameViewModel by activityViewModels<CreateGameViewModel>()

    private fun getSelectedMinefield(): Minefield {
        val mapWidth: TextView? = dialog?.findViewById(R.id.map_width)
        val mapHeight: TextView? = dialog?.findViewById(R.id.map_height)
        val mapMines: TextView? = dialog?.findViewById(R.id.map_mines)

        val width = filterInput(
            mapWidth?.text.toString(),
            MIN_WIDTH
        ).coerceAtMost(MAX_WIDTH)
        val height = filterInput(
            mapHeight?.text.toString(),
            MIN_HEIGHT
        ).coerceAtMost(MAX_HEIGHT)
        val mines = filterInput(
            mapMines?.text.toString(),
            MIN_MINES
        ).coerceAtMost(width * height - 1)

        return Minefield(width, height, mines)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), R.style.MyDialog).apply {
            setTitle(R.string.new_game)
            setView(R.layout.dialog_custom_game)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.start) { _, _ ->
                val minefield = getSelectedMinefield()
                createGameViewModel.updateCustomGameMode(minefield)
                viewModel.startNewGame(Difficulty.Custom)
            }
        }.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (activity is DialogInterface.OnDismissListener) {
            (activity as DialogInterface.OnDismissListener).onDismiss(dialog)
        }
        super.onDismiss(dialog)
    }

    companion object {
        const val MIN_WIDTH = 5
        const val MIN_HEIGHT = 5
        const val MIN_MINES = 3

        const val MAX_WIDTH = 50
        const val MAX_HEIGHT = 50

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

        val TAG = CustomLevelDialogFragment::class.simpleName!!
    }
}
