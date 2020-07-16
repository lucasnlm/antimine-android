package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.models.Minefield
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class FlagAssistantTest {
    @ExperimentalCoroutinesApi
    @Test
    fun testRunAssistant() = runBlockingTest {
        repeat(10) { takeMines ->
            val creator = MinefieldCreator(Minefield(4, 4, 10), Random(200))
            val map = creator.create(50, false)
            val target = map
                .filter { it.hasMine }
                .take(takeMines)
                .onEach { map.filterNeighborsOf(it).forEach { neighbor -> neighbor.isCovered = false } }
                .map { it.id }

            map.filter {
                it.hasMine && map.filterNeighborsOf(it).filter { neighbor -> !neighbor.isCovered || neighbor.hasMine } == map.filterNeighborsOf(it)
            }.map {

            }

            val result = mutableListOf<Int>()
            FlagAssistant(map.toMutableList()).runFlagAssistant().toCollection(result)

            assertEquals("run assistant isolating $takeMines mine(s)", target, result)
        }
    }
}