package dev.lucasnlm.antimine.common.level.solver

import dev.lucasnlm.antimine.core.models.Area

/**
 * Minesweeper deduction solver by Simon Tatham.
 *
 * From: https://www.chiark.greenend.org.uk/~sgtatham/puzzles/js/mines.html
 */
class SimonTathamSolver : GameSolver() {
    override fun trySolve(minefield: MutableList<Area>): Boolean {
        val knownContents = minefield.filter { !it.isCovered }

        while (true) {
            val doneSomething = false

            knownContents.forEach {
            }
        }
    }
}
