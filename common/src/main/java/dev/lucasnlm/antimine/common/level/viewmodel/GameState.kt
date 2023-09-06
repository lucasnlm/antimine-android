package dev.lucasnlm.antimine.common.level.viewmodel

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.preferences.models.Minefield

data class GameState(
    // The ID generated after save this game.
    // It will be zero if it wasn't saved yet.
    val saveId: Long = 0,
    // How many turns in this current session.
    // This value will be reseted if user kill the app.
    val turn: Int = 0,
    // The seed number used to generate this game.
    val seed: Long,
    // Current game difficulty.
    val difficulty: Difficulty,
    // Setup to the current game.
    val minefield: Minefield,
    // All [Area] on current game.
    val field: List<Area>,
    // Current mine counter.
    val mineCount: Int?,
    // Current game durations in milliseconds.
    val duration: Long,
    // How many hints are available.
    val hints: Int,
    // If true, user have already started the game
    // and the minefield has mines in it.
    val hasMines: Boolean,
    // If true, user may use Help feature.
    val useHelp: Boolean,
    // Indicates whether the state is in Game Over
    // Victory or Complete status.
    val isGameCompleted: Boolean,
    // Is true, user may interact with the minefield.
    val isActive: Boolean,
    // If true, the map is being loaded.
    val isLoadingMap: Boolean,
    // If true, a valid game is being created.
    val isCreatingGame: Boolean,
    // If false, it will hide tutorial tip during this session.
    val showTutorial: Boolean,
) {
    /** Indicates whether is a new game */
    val isNewGame = turn == 0 && (saveId == 0L || isLoadingMap || isCreatingGame)

    /** Indicates whether the game is started */
    val isGameStarted = (turn > 0 || saveId != 0L)

    /** Indicates whether show controls text */
    val shouldShowControls = turn < 1 && saveId == 0L && !isLoadingMap && showTutorial
}
