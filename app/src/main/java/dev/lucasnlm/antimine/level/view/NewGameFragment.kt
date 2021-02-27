package dev.lucasnlm.antimine.level.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.models.Event
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.main.view.CardButtonView
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class NewGameFragment : AppCompatDialogFragment() {
    private val gameViewModel by sharedViewModel<GameViewModel>()
    private val themeRepository: IThemeRepository by inject()
    private val savesRepository: ISavesRepository by inject()

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).apply {
            val view: View = LayoutInflater
                .from(context)
                .inflate(R.layout.fragment_new_game, null, false)
                .apply {
                    val beginner = findViewById<CardButtonView>(R.id.beginner).apply {
                        bind(
                            theme = themeRepository.getTheme(),
                            text = R.string.beginner,
                            onAction = {
                                gameViewModel.startNewGame(Difficulty.Beginner)
                                dismissAllowingStateLoss()
                            }
                        )
                    }

                    val intermediate = findViewById<CardButtonView>(R.id.intermediate).apply {
                        bind(
                            theme = themeRepository.getTheme(),
                            text = R.string.intermediate,
                            onAction = {
                                gameViewModel.startNewGame(Difficulty.Intermediate)
                                dismissAllowingStateLoss()
                            }
                        )
                    }

                    val expert = findViewById<CardButtonView>(R.id.expert).apply {
                        bind(
                            theme = themeRepository.getTheme(),
                            text = R.string.expert,
                            onAction = {
                                gameViewModel.startNewGame(Difficulty.Expert)
                                dismissAllowingStateLoss()
                            }
                        )
                    }

                    findViewById<CardButtonView>(R.id.tutorial).apply {
                        bind(
                            theme = themeRepository.getTheme(),
                            text = R.string.tutorial,
                            startIcon = R.drawable.tutorial,
                            onAction = {
                                gameViewModel.eventObserver.postValue(Event.StartTutorial)
                                dismissAllowingStateLoss()
                            }
                        )
                    }

                    lifecycleScope.launchWhenResumed {
                        when (savesRepository.fetchCurrentSave()?.difficulty ?: Difficulty.Beginner) {
                            Difficulty.Standard, Difficulty.Beginner, Difficulty.Custom -> {
                                beginner.requestFocus()
                            }
                            Difficulty.Intermediate -> {
                                intermediate.requestFocus()
                            }
                            Difficulty.Expert -> {
                                expert.requestFocus()
                            }
                        }
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
