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

    object ShowGooglePlayGamesEvent : MainEvent()
}
