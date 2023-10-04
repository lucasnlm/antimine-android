package dev.lucasnlm.antimine.common.io.models

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.preferences.models.Minefield

/**
 * This class is a representation of a save file.
 * @property id The save file id. Null if it's a new save, not yet saved.
 * @property seed The seed used to generate the minefield.
 * @property startDate The date when the game started.
 * @property duration The duration of the game in milliseconds.
 * @property minefield The minefield used in the game.
 * @property difficulty The difficulty of the game.
 * @property firstOpen The first open position of the game.
 * @property status The status of the game.
 * @property field The list of areas of the game.
 * @property actions The number of actions of the game.
 */
data class Save(
    val id: String? = null,
    val seed: Long,
    val startDate: Long,
    val duration: Long,
    val minefield: Minefield,
    val difficulty: Difficulty,
    val firstOpen: FirstOpen,
    val status: SaveStatus,
    val field: List<Area>,
    val actions: Int,
) {
    companion object {
        const val BYTE_SIZE = Long.SIZE_BYTES * 3 + Int.SIZE_BYTES * 5
    }
}
