package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area

interface MinefieldCreator {
    fun createEmpty(): List<Area>

    suspend fun create(safeIndex: Int): List<Area>
}
