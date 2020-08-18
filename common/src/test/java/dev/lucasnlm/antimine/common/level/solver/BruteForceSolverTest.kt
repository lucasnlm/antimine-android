package dev.lucasnlm.antimine.common.level.solver

import dev.lucasnlm.antimine.common.level.logic.MinefieldCreator
import dev.lucasnlm.antimine.common.level.logic.MinefieldHandler
import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.Minefield
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

import kotlin.random.Random

class BruteForceSolverTest {
    private fun handleMinefield(block: (MinefieldHandler, MutableList<Area>) -> Unit) {
        val creator = MinefieldCreator(
            Minefield(9, 9, 12),
            Random(200)
        )
        val minefield = creator.create(40, true).toMutableList()
        val minefieldHandler =
            MinefieldHandler(minefield, false)
        block(minefieldHandler, minefield)
    }

    @Test
    fun isSolvable() {
        handleMinefield { handler, minefield ->
            handler.openAt(40, passive = false, openNeighbors = false)
            val bruteForceSolver = BruteForceSolver()
            assertTrue(bruteForceSolver.trySolve(minefield.toMutableList()))
        }

        handleMinefield { handler, minefield ->
            handler.openAt(0, passive = false, openNeighbors = false)
            val bruteForceSolver = BruteForceSolver()
            assertFalse(bruteForceSolver.trySolve(minefield.toMutableList()))
        }
    }
}
