package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.models.Area

class BruteForceSolver(
    private val minefield: MutableList<Area>
) {
    private val minefieldHandler = MinefieldHandler(minefield, false)

    fun isSolvable(): Boolean {
        do {
            val initialMap = minefield.filter { !it.isCovered && it.minesAround != 0 }
            initialMap.forEach {
                minefieldHandler.openOrFlagNeighborsOf(it.id)
            }
        } while (initialMap != minefield.filter { !it.isCovered && it.minesAround != 0 })

        return minefield.count { it.hasMine && !it.mark.isFlag()} == 0
    }
}
