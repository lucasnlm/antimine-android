package dev.lucasnlm.antimine.main.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dev.lucasnlm.antimine.GameActivity
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.viewmodel.StatelessViewModel
import dev.lucasnlm.antimine.preferences.IPreferencesRepository

class MainViewModel(
    private val context: Context,
    private val preferencesRepository: IPreferencesRepository,
) : StatelessViewModel<MainEvent>() {
    override fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.ContinueGameEvent -> continueGame()
            is MainEvent.StartNewGameEvent -> continueGame(event.difficulty)
            is MainEvent.ShowCustomDifficultyDialogEvent -> showCustomDifficultyDialogEvent()
            is MainEvent.StartTutorialEvent -> startTutorial()
            is MainEvent.GoToSettingsPageEvent -> goToSettingsPageEvent()
            is MainEvent.ShowControlsEvent -> showControlsEvent()
            is MainEvent.ShowGooglePlayGamesEvent -> showGooglePlayGames()
        }
    }

    private fun showCustomDifficultyDialogEvent() {
        sendSideEffect(MainEvent.ShowCustomDifficultyDialogEvent)
    }

    private fun goToSettingsPageEvent() {
        sendSideEffect(MainEvent.GoToSettingsPageEvent)
    }

    private fun showControlsEvent() {
        sendSideEffect(MainEvent.ShowControlsEvent)
    }

    private fun showGooglePlayGames() {
        sendSideEffect(MainEvent.ShowGooglePlayGamesEvent)
    }

    private fun continueGame(difficulty: Difficulty? = null) {
        val intent = Intent(context, GameActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK

            difficulty?.let {
                val bundle = Bundle().apply {
                    putSerializable(GameActivity.DIFFICULTY, it)
                }
                putExtras(bundle)
            }
        }

        context.startActivity(intent)
    }

    private fun startTutorial() {
        val intent = Intent(context, GameActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val bundle = Bundle().apply {
                putBoolean(GameActivity.START_TUTORIAL, true)
            }

            putExtras(bundle)
        }
        context.startActivity(intent)
    }
}
