package dev.lucasnlm.antimine.common.level.solver

import dev.lucasnlm.antimine.common.level.logic.MinefieldCreatorImpl
import dev.lucasnlm.antimine.common.level.logic.MinefieldHandler
import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.Thread.sleep

class LimitedCheckNeighborsSolverTest {
    private fun handleMinefield(block: (MinefieldHandler) -> Unit) =
        runTest {
            val creator =
                MinefieldCreatorImpl(
                    Minefield(9, 9, 12),
                    200,
                )
            val minefield = creator.create(40).toMutableList()
            val minefieldHandler = MinefieldHandler(minefield, useQuestionMark = false, individualActions = false)
            block(minefieldHandler)
        }

    @Test
    fun isSolvable() {
        handleMinefield { handler ->
            handler.openAt(40, passive = false, openNeighbors = true)
            val bruteForceSolver = LimitedCheckNeighborsSolver()
            assertTrue(bruteForceSolver.trySolve(handler.result().toMutableList()))
        }

        handleMinefield { handler ->
            handler.openAt(5, passive = false, openNeighbors = true)
            val bruteForceSolver = LimitedCheckNeighborsSolver()
            assertFalse(bruteForceSolver.trySolve(handler.result().toMutableList()))
        }
    }

    @Test
    fun shouldntKeepTryingAfterTimeout() {
        handleMinefield { handler ->
            handler.openAt(40, passive = false, openNeighbors = false)
            val bruteForceSolver = LimitedCheckNeighborsSolver(1000L)
            assertTrue(bruteForceSolver.keepTrying())
        }

        handleMinefield { handler ->
            handler.openAt(0, passive = false, openNeighbors = false)
            val bruteForceSolver = LimitedCheckNeighborsSolver(50)
            sleep(100)
            assertFalse(bruteForceSolver.keepTrying())
        }
    }
}
