package dev.lucasnlm.antimine.main.viewmodel

import android.content.Intent
import dev.lucasnlm.antimine.core.models.Difficulty

sealed class MainEvent {
    object ContinueGameEvent : MainEvent()

    data class StartNewGameEvent(
        val difficulty: Difficulty,
    ) : MainEvent()

    object ShowCustomDifficultyDialogEvent : MainEvent()

    object StartTutorialEvent : MainEvent()

    object GoToMainPageEvent : MainEvent()

    data class OpenActivity(
        val intent: Intent,
    ) : MainEvent()

    object GoToSettingsPageEvent : MainEvent()

    object ShowControlsEvent : MainEvent()

    object Recreate : MainEvent()

    object ShowGooglePlayGamesEvent : MainEvent()

    data class FetchCloudSave(
        val playGamesId: String,
    ) : MainEvent()
}
