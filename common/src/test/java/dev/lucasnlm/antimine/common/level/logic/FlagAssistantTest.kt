package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.logic.MinefieldExt.filterNeighborsOf
import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
class FlagAssistantTest {
    private fun testCase(
        seed: Long,
        expectedFlagMap: List<Int>,
    ) = runTest {
        val creator =
            MinefieldCreatorImpl(
                Minefield(8, 8, 25),
                seed,
            )

        val map = creator.create(50).toMutableList()

        map.filter { it.hasMine }
            .toList()
            .shuffled(Random(seed))
            .take(5)
            .forEach {
                map.filterNeighborsOf(it)
                    .forEach { neighbor ->
                        map[neighbor.id] = neighbor.copy(isCovered = false)
                    }
            }

        val actual =
            FlagAssistant(map.toMutableList()).run {
                runFlagAssistant()
                result().map { it.mark.ordinal }
            }

        assertEquals(expectedFlagMap, actual)
    }

    @Test
    fun testRunAssistantCase1() =
        runTest {
            testCase(
                seed = 200,
                expectedFlagMap =
                    listOf(
                        0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0,
                        1, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 1, 0, 0, 0,
                        0, 0, 0, 0, 1, 0, 0, 0,
                        1, 0, 0, 0, 1, 0, 0, 0,
                        0, 0, 0, 0, 1, 1, 0, 0,
                    ),
            )
        }

    @Test
    fun testRunAssistantCase2() =
        runTest {
            testCase(
                seed = 250,
                expectedFlagMap =
                    listOf(
                        0, 0, 0, 0, 0, 0, 0, 1,
                        0, 0, 0, 0, 1, 1, 1, 1,
                        0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0,
                        1, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 1, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0,
                    ),
            )
        }
}
