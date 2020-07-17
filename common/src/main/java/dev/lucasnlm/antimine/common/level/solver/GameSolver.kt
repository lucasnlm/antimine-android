package dev.lucasnlm.antimine.common.level.solver

import dev.lucasnlm.antimine.common.level.models.Area

abstract class GameSolver(
    protected val minefield: MutableList<Area>
) {
    abstract fun isSolvable(): Boolean
}
