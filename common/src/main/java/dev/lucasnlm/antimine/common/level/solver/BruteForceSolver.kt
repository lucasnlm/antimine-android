package dev.lucasnlm.antimine.common.level.solver

import dev.lucasnlm.antimine.common.level.logic.MinefieldHandler
import dev.lucasnlm.antimine.core.models.Area

open class BruteForceSolver : GameSolver() {
    override fun trySolve(minefield: MutableList<Area>): Boolean {
        val minefieldHandler = MinefieldHandler(minefield, false)

        do {
            val initialMap = minefield.filter { !it.isCovered && it.minesAround != 0 }
            initialMap.forEach { minefieldHandler.openOrFlagNeighborsOf(it.id) }
        } while (initialMap != minefield.filter { !it.isCovered && it.minesAround != 0 })

        return minefield.count { it.hasMine && !it.mark.isFlag() } == 0
    }
}
