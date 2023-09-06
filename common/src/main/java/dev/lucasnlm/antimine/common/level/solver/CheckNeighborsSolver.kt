package dev.lucasnlm.antimine.common.level.solver

import dev.lucasnlm.antimine.common.level.logic.MinefieldHandler
import dev.lucasnlm.antimine.core.models.Area

/**
 * Brute force solver that try solve a minefield checking
 * all neighbors.
 *
 * Bad point:
 *  - Solves only easy minefields.
 */
open class CheckNeighborsSolver : GameSolver() {
    override fun trySolve(minefield: MutableList<Area>): Boolean {
        val minefieldHandler =
            MinefieldHandler(
                field = minefield,
                useQuestionMark = false,
                individualActions = true,
            )

        do {
            val initialMap = minefield.filter { !it.isCovered && it.minesAround != 0 }
            initialMap.forEach { minefieldHandler.openOrFlagNeighborsOf(it.id) }
        } while (initialMap != minefield.filter { !it.isCovered && it.minesAround != 0 })

        return minefield.count { it.hasMine && !it.mark.isFlag() } == 0
    }
}
