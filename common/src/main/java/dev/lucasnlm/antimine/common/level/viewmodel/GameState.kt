package dev.lucasnlm.antimine.common.level.viewmodel

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.preferences.models.Minefield

data class GameState(
    val saveId: Long = 0,
    val turn: Int = 0,
    val seed: Long,
    val difficulty: Difficulty,
    val minefield: Minefield,
    val field: List<Area>,
    val mineCount: Int,
    val timestamp: Long,
    val tips: Int,

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
)
