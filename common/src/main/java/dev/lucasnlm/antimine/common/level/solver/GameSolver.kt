package dev.lucasnlm.antimine.common.level.solver

import dev.lucasnlm.antimine.core.models.Area

abstract class GameSolver {
    /**
     * If true it may keep iterating on this algorithm.
     */
    open fun keepTrying() = true

    /**
     * Try solve the given [minefield].
     * Returns true if it's solvable or false otherwise.
     */
    abstract fun trySolve(minefield: MutableList<Area>): Boolean
}
