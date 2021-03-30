package dev.lucasnlm.antimine.common.level.viewmodel

import dev.lucasnlm.antimine.core.models.Area

sealed class GameEvent {
    data class UpdateMinefield(
        val field: List<Area>,
    ) : GameEvent()

    data class UpdateTime(
        val time: Long
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

    object ConsumeTip : GameEvent()

    object ContinueGame : GameEvent()

    object ShareGame : GameEvent()

    object ShowNewGameDialog : GameEvent()

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
    ) : GameEvent()

    data class GameCompleteDialog(
        val delayToShow: Long,
        val totalMines: Int,
        val rightMines: Int,
        val timestamp: Long,
        val receivedTips: Int,
    ) : GameEvent()
}
