package dev.lucasnlm.antimine.main.viewmodel

import dev.lucasnlm.antimine.core.models.Difficulty

sealed class MainEvent {
    object ContinueGameEvent : MainEvent()

    data class StartNewGameEvent(
        val difficulty: Difficulty,
    ) : MainEvent()

    object ShowCustomDifficultyDialogEvent : MainEvent()

    object StartTutorialEvent : MainEvent()

    object GoToSettingsPageEvent : MainEvent()

    object ShowControlsEvent : MainEvent()

    object Recreate : MainEvent()

    object ShowGooglePlayGamesEvent : MainEvent()

    data class FetchCloudSave(
        val playGamesId: String,
    ) : MainEvent()
}
