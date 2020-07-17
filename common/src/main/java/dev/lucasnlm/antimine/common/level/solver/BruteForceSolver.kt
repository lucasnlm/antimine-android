package dev.lucasnlm.antimine.common.level.solver

import dev.lucasnlm.antimine.common.level.logic.MinefieldHandler
import dev.lucasnlm.antimine.common.level.models.Area

class BruteForceSolver(
    minefield: MutableList<Area>
): GameSolver(minefield) {
    private val minefieldHandler =
        MinefieldHandler(minefield, false)

    override fun isSolvable(): Boolean {
        do {
            val initialMap = minefield.filter { !it.isCovered && it.minesAround != 0 }
            initialMap.forEach {
                minefieldHandler.openOrFlagNeighborsOf(it.id)
            }
        } while (initialMap != minefield.filter { !it.isCovered && it.minesAround != 0 })

        return minefield.count { it.hasMine && !it.mark.isFlag()} == 0
    }
}
