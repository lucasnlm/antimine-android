package dev.lucasnlm.antimine.common.level.solver

import dev.lucasnlm.antimine.core.models.Area

/**
 * Minesweeper solver by Petersohn.
 * https://github.com/petersohn/mine-solver/
 */
class PetersohnSolver : GameSolver() {
    override fun trySolve(minefield: MutableList<Area>): Boolean {
        return false
    }
}
