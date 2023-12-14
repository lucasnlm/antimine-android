package dev.lucasnlm.antimine.custom

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.custom.viewmodel.CreateGameViewModel
import dev.lucasnlm.antimine.custom.viewmodel.CustomEvent
import dev.lucasnlm.antimine.databinding.DialogCustomGameBinding
import dev.lucasnlm.antimine.main.viewmodel.MainEvent
import dev.lucasnlm.antimine.main.viewmodel.MainViewModel
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import dev.lucasnlm.antimine.i18n.R as i18n

class CustomLevelDialogFragment : AppCompatDialogFragment() {
    private val gameViewModel by sharedViewModel<MainViewModel>()
    private val createGameViewModel by viewModel<CreateGameViewModel>()
    private val preferencesRepository: PreferencesRepository by inject()

    private val binding: DialogCustomGameBinding by lazy {
        val layoutInflater = LayoutInflater.from(context)
        DialogCustomGameBinding.inflate(layoutInflater, null, false)
    }

    private fun getSelectedMinefield(): Minefield {
        val width = filterInput(binding.mapWidth.text.toString(), MIN_WIDTH).coerceAtMost(MAX_WIDTH)
        val height = filterInput(binding.mapHeight.text.toString(), MIN_HEIGHT).coerceAtMost(MAX_HEIGHT)
        val maxMines = width * height - MIN_SAFE_AREA
        val mines = filterInput(binding.mapMines.text.toString(), MIN_MINES).coerceAtMost(maxMines)
        val seedValue = binding.seed.text.toString().toLongOrNull()

        return Minefield(width, height, mines, seedValue)
    }

    private fun createView(): View {
        createGameViewModel.singleState().let { state ->
            binding.run {
                mapWidth.setText(state.width.toString())
                mapHeight.setText(state.height.toString())
                mapMines.setText(state.mines.toString())
                seed.setText("")

                mapWidth.checkLimit(MIN_WIDTH, MAX_WIDTH)
                mapHeight.checkLimit(MIN_HEIGHT, MAX_HEIGHT)
                mapMines.checkProportionOnChange()
            }
        }

        return binding.root
    }

    private fun TextInputEditText.checkProportion() {
        val width = binding.mapWidth.text.toString().toIntOrNull()
        val height = binding.mapHeight.text.toString().toIntOrNull()
        val current = text.toString().toIntOrNull()
        error =
            if (current != null && width != null && height != null) {
                val minMines = (width * height * 0.5 - MIN_SAFE_AREA).toInt().coerceAtLeast(1)
                if (current <= minMines) {
                    null
                } else {
                    val maxProportion = width * height * 0.75
                    if (current >= maxProportion) {
                        getString(i18n.string.proportion_too_high)
                    } else {
                        null
                    }
                }
            } else {
                null
            }
    }

    private fun TextInputEditText.checkProportionOnChange() {
        doAfterTextChanged {
            checkProportion()
        }
    }

    private fun TextInputEditText.checkLimit(
        min: Int,
        max: Int,
    ) {
        doAfterTextChanged {
            if (it?.isNotBlank() == true) {
                val current = text.toString().toIntOrNull()
                if (current != null) {
                    if (current > max) {
                        error = getString(i18n.string.value_limit_max, max)
                    } else if (current < min) {
                        error = getString(i18n.string.value_limit_min, min)
                    }
                } else {
                    error = null
                }
            } else {
                error = null
            }

            binding.mapMines.checkProportion()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(i18n.string.new_game)
            setView(createView())
            setNegativeButton(i18n.string.cancel, null)
            setPositiveButton(i18n.string.start) { _, _ ->
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
        const val MIN_SAFE_AREA = 9
        const val MAX_WIDTH = 100
        const val MAX_HEIGHT = 100

        private fun filterInput(
            target: String,
            min: Int,
        ): Int {
            return runCatching { Integer.valueOf(target) }
                .getOrDefault(min)
                .coerceAtLeast(min)
        }

        val TAG = CustomLevelDialogFragment::class.simpleName!!
    }
}
