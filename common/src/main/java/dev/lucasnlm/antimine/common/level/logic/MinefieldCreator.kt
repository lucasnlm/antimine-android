package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area

interface MinefieldCreator {
    /**
     * Creates an empty minefield.
     */
    fun createEmpty(): List<Area>

    /**
     * Creates a minefield with the given safe index.
     * The neighbors of the safe index won't have mines.
     */
    suspend fun create(safeIndex: Int): List<Area>
}
