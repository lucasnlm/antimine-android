package dev.lucasnlm.antimine.common.level.solver

import dev.lucasnlm.antimine.common.level.logic.MinefieldCreatorImpl
import dev.lucasnlm.antimine.common.level.logic.MinefieldHandler
import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckNeighborsSolverTest {
    private fun handleMinefield(block: (MinefieldHandler) -> Unit) =
        runTest {
            val creator =
                MinefieldCreatorImpl(
                    Minefield(9, 9, 12),
                    200,
                )
            val minefield = creator.create(40).toMutableList()
            val minefieldHandler =
                MinefieldHandler(minefield, useQuestionMark = false, individualActions = false)
            block(minefieldHandler)
        }

    @Test
    fun isSolvable() {
        handleMinefield { handler ->
            handler.openAt(40, passive = false, openNeighbors = true)
            val bruteForceSolver = CheckNeighborsSolver()
            assertTrue(bruteForceSolver.trySolve(handler.result().toMutableList()))
        }

        handleMinefield { handler ->
            handler.openAt(0, passive = false, openNeighbors = false)
            val bruteForceSolver = CheckNeighborsSolver()
            assertFalse(bruteForceSolver.trySolve(handler.result().toMutableList()))
        }
    }
}
