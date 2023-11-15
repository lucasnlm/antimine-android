package dev.lucasnlm.antimine.main.viewmodel

import android.content.Intent
import dev.lucasnlm.antimine.core.models.Difficulty

sealed class MainEvent {
    data object ContinueGameEvent : MainEvent()

    data class StartNewGameEvent(
        val difficulty: Difficulty,
    ) : MainEvent()

    data object ShowCustomDifficultyDialogEvent : MainEvent()

    data object StartTutorialEvent : MainEvent()

    data object StartLanguageEvent : MainEvent()

    data class OpenActivity(
        val intent: Intent,
    ) : MainEvent()

    data object ShowControlsEvent : MainEvent()

    data object Recreate : MainEvent()

    data object ShowGooglePlayGamesEvent : MainEvent()

    data class FetchCloudSave(
        val playGamesId: String,
    ) : MainEvent()
}
