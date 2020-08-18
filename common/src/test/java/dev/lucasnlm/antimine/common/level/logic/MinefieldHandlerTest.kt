package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.Mark
import dev.lucasnlm.antimine.common.level.models.Minefield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MinefieldHandlerTest {
    private fun handleMinefield(
        useQuestionMark: Boolean = false,
        useSafeZone: Boolean = false,
        block: (MinefieldHandler, MutableList<Area>) -> Unit
    ) {
        val creator = MinefieldCreator(Minefield(4, 4, 9), Random(200))
        val minefield = creator.create(10, useSafeZone).toMutableList()
        val minefieldHandler = MinefieldHandler(minefield, useQuestionMark)
        block(minefieldHandler, minefield)
    }

    @Test
    fun testOpenArea() {
        handleMinefield { handler, minefield ->
            assertTrue(minefield[3].isCovered)
            handler.openAt(3, false, openNeighbors = false)
            assertFalse(minefield[3].isCovered)
            assertEquals(Mark.None, minefield[3].mark)
        }
    }

    @Test
    fun testOpenAreaWithSafeZone() {
        handleMinefield(useSafeZone = true) { handler, minefield ->
            assertTrue(minefield[3].isCovered)
            handler.openAt(3, false, openNeighbors = false)
            assertFalse(minefield[3].isCovered)
            assertEquals(Mark.None, minefield[3].mark)
        }
    }

    @Test
    fun testTurnOffHighlight() {
        handleMinefield { handler, minefield ->
            minefield[3] = minefield[3].copy(highlighted = true)
            handler.turnOffAllHighlighted()
            assertFalse(minefield[3].highlighted)
        }
    }

    @Test
    fun testRemoveMark() {
        handleMinefield { handler, minefield ->
            minefield[3] = minefield[3].copy(mark = Mark.Flag)
            handler.removeMarkAt(3)
            assertTrue(minefield[3].mark == Mark.PurposefulNone)
            assertTrue(minefield[3].mark.isNone())
        }
    }

    @Test
    fun testSwitchMarkWithoutQuestionMark() {
        handleMinefield { handler, minefield ->
            assertTrue(minefield[3].mark.isNone())
            handler.switchMarkAt(3)
            assertTrue(minefield[3].mark.isFlag())
            handler.switchMarkAt(3)
            assertTrue(minefield[3].mark.isNone())
        }
    }

    @Test
    fun testSwitchMarkWithQuestionMark() {
        handleMinefield(useQuestionMark = true) { handler, minefield ->
            assertTrue(minefield[3].mark.isNone())
            handler.switchMarkAt(3)
            assertTrue(minefield[3].mark.isFlag())
            handler.switchMarkAt(3)
            assertTrue(minefield[3].mark.isQuestion())
            handler.switchMarkAt(3)
            assertTrue(minefield[3].mark.isNone())
        }
    }

    @Test
    fun testHighlight() {
        handleMinefield(useQuestionMark = true) { handler, minefield ->
            assertEquals(0, minefield.count { it.highlighted })

            // Before open
            handler.highlightAt(5)
            assertEquals(0, minefield.count { it.highlighted })

            // After Open
            handler.openAt(5, false, openNeighbors = false)
            val target = minefield.first { it.minesAround != 0 }
            handler.highlightAt(target.id)
            assertEquals(5, minefield.count { it.highlighted })
            assertEquals(listOf(0, 1, 4, 8, 9), minefield.filter { it.highlighted }.map { it.id }.toList())
        }
    }

    @Test
    fun testOpenNeighborsClosedArea() {
        handleMinefield { handler, minefield ->
            handler.openOrFlagNeighborsOf(3)
            assertEquals(0, minefield.count { !it.isCovered })
        }
    }

    @Test
    fun testOpenNeighbors() {
        handleMinefield { handler, minefield ->
            handler.openAt(5, false, openNeighbors = true)
            handler.openOrFlagNeighborsOf(5)
            assertEquals(9, minefield.count { !it.isCovered })
        }
    }

    @Test
    fun testOpenNeighborsWithFlags() {
        handleMinefield { handler, minefield ->
            handler.openAt(5, false, openNeighbors = true)
            val neighbors = minefield.filterNeighborsOf(minefield.first { it.id == 5 })
            neighbors.filter { it.hasMine }.forEach { handler.switchMarkAt(it.id) }
            handler.openOrFlagNeighborsOf(5)
            assertEquals(4, minefield.count { !it.isCovered })
            assertEquals(3, neighbors.count { !it.isCovered })
        }
    }

    @Test
    fun testOpenNeighborsWithQuestionMarks() {
        handleMinefield(useQuestionMark = true) { handler, minefield ->
            handler.openAt(5, false, openNeighbors = true)
            val neighbors = minefield.filterNeighborsOf(minefield.first { it.id == 5 })
            neighbors
                .filter { it.hasMine }
                .forEach {
                    handler.switchMarkAt(it.id)
                    handler.switchMarkAt(it.id)
                }
            handler.openOrFlagNeighborsOf(5)
            assertEquals(4, minefield.count { !it.isCovered })
            assertEquals(3, neighbors.count { !it.isCovered })
        }
    }
}
