package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area

interface MinefieldCreator {
    fun createEmpty(): List<Area>
    fun create(safeIndex: Int, safeZone: Boolean): List<Area>
}
