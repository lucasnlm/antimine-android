package dev.lucasnlm.antimine.custom

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.custom.viewmodel.CreateGameViewModel
import dev.lucasnlm.antimine.custom.viewmodel.CustomEvent
import dev.lucasnlm.antimine.databinding.DialogCustomGameBinding
import dev.lucasnlm.antimine.main.viewmodel.MainEvent
import dev.lucasnlm.antimine.main.viewmodel.MainViewModel
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CustomLevelDialogFragment : AppCompatDialogFragment() {
    private val gameViewModel by sharedViewModel<MainViewModel>()
    private val createGameViewModel by viewModel<CreateGameViewModel>()
    private val preferencesRepository: IPreferencesRepository by inject()

    private lateinit var binding: DialogCustomGameBinding

    private fun getSelectedMinefield(): Minefield {
        val width = filterInput(binding.mapWidth.text.toString(), MIN_WIDTH).coerceAtMost(MAX_WIDTH)
        val height = filterInput(binding.mapHeight.text.toString(), MIN_HEIGHT).coerceAtMost(MAX_HEIGHT)
        val maxMines = width * height - 9
        val mines = filterInput(binding.mapMines.text.toString(), MIN_MINES).coerceAtMost(maxMines)
        val seedValue = binding.seed.text.toString().toLongOrNull()

        return Minefield(width, height, mines, seedValue)
    }

    @SuppressLint("InflateParams")
    private fun createView(): View {
        val layoutInflater = LayoutInflater.from(context)
        binding = DialogCustomGameBinding.inflate(layoutInflater, null, false)

        createGameViewModel.singleState().let { state ->
            binding.run {
                mapWidth.setText(state.width.toString())
                mapHeight.setText(state.height.toString())
                mapMines.setText(state.mines.toString())
                seed.setText("")
            }
        }

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.new_game)
            setView(createView())
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.start) { _, _ ->
                val minefield = getSelectedMinefield()
                preferencesRepository.setCompleteTutorial(true)
                createGameViewModel.sendEvent(CustomEvent.UpdateCustomGameEvent(minefield))
                gameViewModel.sendEvent(MainEvent.StartNewGameEvent(Difficulty.Custom))
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
