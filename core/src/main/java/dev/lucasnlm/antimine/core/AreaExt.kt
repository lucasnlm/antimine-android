package dev.lucasnlm.antimine.core

import dev.lucasnlm.antimine.core.models.Area

object AreaExt {
    fun Area.getNeighborIdAtPos(
        field: List<Area>,
        dx: Int,
        dy: Int,
    ): Int {
        val posX = posX + dx
        val posY = posY + dy

        return neighborsIds.firstOrNull {
            val neighbor = field[it]
            neighbor.posX == posX && neighbor.posY == posY
        } ?: -1
    }
}
