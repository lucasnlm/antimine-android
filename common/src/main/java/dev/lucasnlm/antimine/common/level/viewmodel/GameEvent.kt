package dev.lucasnlm.antimine.common.level.viewmodel

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.preferences.models.Action

/**
 * Events that can be triggered by the game.
 */
sealed class GameEvent {
    /**
     * Update the minefield.
     * @property field The new minefield.
     */
    data class UpdateMinefield(
        val field: List<Area>,
    ) : GameEvent()

    /**
     * Update the mine counter.
     * @property time the new time in seconds.
     */
    data class UpdateTime(
        val time: Long,
    ) : GameEvent()

    /**
     * Start a new game.
     * @property newState The new game state.
     */
    data class NewGame(
        val newState: GameState,
    ) : GameEvent()

    /**
     * Update the save id.
     */
    data class UpdateSaveId(
        val saveId: String?,
    ) : GameEvent()

    /**
     * Activate or deactivate the game.
     * @property active If true, activate the game.
     */
    data class SetGameActivation(
        val active: Boolean,
    ) : GameEvent()

    /**
     * Give a tip to the user.
     */
    data object GiveMoreTip : GameEvent()

    /**
     * Consume a tip from the user.
     */
    data object ConsumeTip : GameEvent()

    /**
     * Continue a game after game over.
     */
    data object ContinueGame : GameEvent()

    /**
     * Show a dialog to start a new game.
     */
    data object ShowNewGameDialog : GameEvent()

    /**
     * Show a dialog to start a new game.
     */
    data object LoadingNewGame : GameEvent()

    /**
     * Indicates that the engine is ready.
     */
    data object EngineReady : GameEvent()

    /**
     * Indicates that the actors are loaded.
     */
    data object ActorLoaded : GameEvent()

    /**
     * Indicates No Guess mode failed.
     */
    data object ShowNoGuessFailWarning : GameEvent()

    /**
     * Indicates that the game is being created.
     */
    data object CreatingGameEvent : GameEvent()

    /**
     * Change the selected action (flag or open).
     * @property action The new action.
     */
    data class ChangeSelectedAction(
        val action: Action?,
    ) : GameEvent()

    /**
     * Show the Victory dialog.
     */
    data class VictoryDialog(
        val delayToShow: Long,
        val totalMines: Int,
        val rightMines: Int,
        val timestamp: Long,
        val receivedTips: Int,
    ) : GameEvent()

    /**
     * Show the Game Over dialog.
     */
    data class GameOverDialog(
        val delayToShow: Long,
        val totalMines: Int,
        val rightMines: Int,
        val timestamp: Long,
        val receivedTips: Int,
        val turn: Int,
    ) : GameEvent()

    /**
     * Show the Game Complete dialog.
     * Game finished with errors.
     */
    data class GameCompleteDialog(
        val delayToShow: Long,
        val totalMines: Int,
        val rightMines: Int,
        val timestamp: Long,
        val receivedTips: Int,
        val turn: Int,
    ) : GameEvent()
}
