package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class FlagAssistantTest {
    @Test
    fun testRunAssistant() = runBlockingTest {
        repeat(20) { takeMines ->
            val creator = MinefieldCreator(
                Minefield(8, 8, 25),
                Random(200)
            )
            val map = creator.create(50, false).toMutableList()

            map.filter { it.hasMine }
                .take(takeMines)
                .forEach {
                    map.filterNeighborsOf(it)
                        .forEach { neighbor ->
                            map[neighbor.id] = neighbor.copy(isCovered = false)
                        }
                }

            val actual = FlagAssistant(map.toMutableList()).run {
                runFlagAssistant()
                result()
            }

            val expected = map
                .filter { it.hasMine }
                .mapNotNull {
                    val neighbors = map.filterNeighborsOf(it)
                    val neighborsCount = neighbors.count()
                    val revealedNeighborsCount = neighbors.count { neighbor ->
                        !neighbor.isCovered || (neighbor.hasMine && neighbor.mark.isFlag())
                    }
                    if (neighborsCount == revealedNeighborsCount) it.id else null
                }

            assertEquals("run assistant isolating $takeMines mine(s)", expected, actual)
        }

        repeat(20) { takeMines ->
            val seed = 10 * takeMines
            val creator = MinefieldCreator(
                Minefield(8, 8, 25),
                Random(seed)
            )
            val map = creator.create(50, false).toMutableList()

            map.filter { it.hasMine }
                .take(takeMines)
                .forEach {
                    map.filterNeighborsOf(it)
                        .forEach { neighbor ->
                            map[neighbor.id] = neighbor.copy(isCovered = false)
                        }
                }

            val actual = FlagAssistant(map.toMutableList()).run {
                runFlagAssistant()
                result()
            }

            val expected = map
                .filter { it.hasMine }
                .mapNotNull {
                    val neighbors = map.filterNeighborsOf(it)
                    val neighborsCount = neighbors.count()
                    val revealedNeighborsCount = neighbors.count { neighbor ->
                        !neighbor.isCovered || (neighbor.hasMine && neighbor.mark.isFlag())
                    }
                    if (neighborsCount == revealedNeighborsCount) it.id else null
                }

            assertEquals("run assistant isolating $takeMines mine(s) and seed $seed", expected, actual)
        }
    }
}
