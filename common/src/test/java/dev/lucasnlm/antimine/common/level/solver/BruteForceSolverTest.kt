package dev.lucasnlm.antimine.common.level.solver

import dev.lucasnlm.antimine.common.level.logic.MinefieldCreator
import dev.lucasnlm.antimine.common.level.logic.MinefieldHandler
import dev.lucasnlm.antimine.preferences.models.Minefield
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

import kotlin.random.Random

class BruteForceSolverTest {
    private fun handleMinefield(block: (MinefieldHandler) -> Unit) {
        val creator = MinefieldCreator(
            Minefield(9, 9, 12),
            Random(200)
        )
        val minefield = creator.create(40, true).toMutableList()
        val minefieldHandler =
            MinefieldHandler(minefield, false)
        block(minefieldHandler)
    }

    @Test
    fun isSolvable() {
        handleMinefield { handler ->
            handler.openAt(40, passive = false, openNeighbors = true)
            val bruteForceSolver = BruteForceSolver()
            assertTrue(bruteForceSolver.trySolve(handler.result().toMutableList()))
        }

        handleMinefield { handler ->
            handler.openAt(0, passive = false, openNeighbors = false)
            val bruteForceSolver = BruteForceSolver()
            assertFalse(bruteForceSolver.trySolve(handler.result().toMutableList()))
        }
    }
}
