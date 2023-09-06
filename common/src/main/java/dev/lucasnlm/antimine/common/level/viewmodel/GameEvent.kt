package dev.lucasnlm.antimine.common.level.viewmodel

import dev.lucasnlm.antimine.core.models.Area

sealed class GameEvent {
    data class UpdateMinefield(
        val field: List<Area>,
    ) : GameEvent()

    data class UpdateTime(
        val time: Long,
    ) : GameEvent()

    data class NewGame(
        val newState: GameState,
    ) : GameEvent()

    data class UpdateSave(
        val saveId: Long,
    ) : GameEvent()

    data class SetGameActivation(
        val active: Boolean,
    ) : GameEvent()

    data object GiveMoreTip : GameEvent()

    data object ConsumeTip : GameEvent()

    data object ContinueGame : GameEvent()

    data object ShowNewGameDialog : GameEvent()

    data object LoadingNewGame : GameEvent()

    data object EngineReady : GameEvent()

    data object ShowNoGuessFailWarning : GameEvent()

    data object CreatingGameEvent : GameEvent()

    data class VictoryDialog(
        val delayToShow: Long,
        val totalMines: Int,
        val rightMines: Int,
        val timestamp: Long,
        val receivedTips: Int,
    ) : GameEvent()

    data class GameOverDialog(
        val delayToShow: Long,
        val totalMines: Int,
        val rightMines: Int,
        val timestamp: Long,
        val receivedTips: Int,
        val turn: Int,
    ) : GameEvent()

    data class GameCompleteDialog(
        val delayToShow: Long,
        val totalMines: Int,
        val rightMines: Int,
        val timestamp: Long,
        val receivedTips: Int,
        val turn: Int,
    ) : GameEvent()
}
