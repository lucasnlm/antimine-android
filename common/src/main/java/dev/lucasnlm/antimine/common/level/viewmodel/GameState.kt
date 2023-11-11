package dev.lucasnlm.antimine.common.level.viewmodel

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.Minefield

/**
 * Represents the current state of the game.
 * @property saveId The save id of current game.
 * @property turn How many turns in this current session.
 * @property seed The seed number used to generate this game.
 * @property difficulty Current game difficulty.
 * @property minefield Setup to the current game.
 * @property field Current game field.
 * @property mineCount Current mine counter.
 * @property selectedAction Current action selected by user.
 * @property duration Current game durations in seconds.
 * @property hints How many hints are available.
 * @property hasMines If true, user have already started the game.
 * @property useHelp If true, user may use help.
 * @property isGameCompleted If true, the game is completed.
 * @property isActive If true, user may interact with the minefield.
 * @property isEngineLoading If true, the map is being loaded.
 * @property isActorsLoaded If true, the actors are loaded.
 * @property isCreatingGame If true, a valid game is being created.
 * @property showTutorial If false, it will hide tutorial tip during this session.
 */
data class GameState(
    val saveId: String? = null,
    val turn: Int = 0,
    val seed: Long,
    val difficulty: Difficulty,
    val minefield: Minefield,
    val field: List<Area>,
    val mineCount: Int?,
    val selectedAction: Action? = null,
    val duration: Long,
    val hints: Int,
    val hasMines: Boolean,
    val useHelp: Boolean,
    val isGameCompleted: Boolean,
    val isActive: Boolean,
    val isEngineLoading: Boolean,
    val isActorsLoaded: Boolean,
    val isCreatingGame: Boolean,
    val showTutorial: Boolean,
) {
    /** Indicates whether is a new game */
    val isNewGame = turn == 0 && (saveId == null || isEngineLoading || isCreatingGame)

    /** Indicates whether the game is started */
    val isGameStarted = (turn > 0 || saveId != null)

    /** Indicates whether show controls text */
    val shouldShowControls = turn < 1 && saveId == null && !isEngineLoading && showTutorial
}
