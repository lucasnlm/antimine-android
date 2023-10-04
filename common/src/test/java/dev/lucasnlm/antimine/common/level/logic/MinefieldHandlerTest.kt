package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.logic.MinefieldExt.filterNeighborsOf
import dev.lucasnlm.antimine.core.models.Mark
import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MinefieldHandlerTest {
    private fun handleMinefield(
        useQuestionMark: Boolean = false,
        block: (MinefieldHandler) -> Unit,
    ) = runTest {
        val creator = MinefieldCreatorImpl(Minefield(4, 4, 9), 200)
        val minefield = creator.create(10).toMutableList()
        val minefieldHandler = MinefieldHandler(minefield, useQuestionMark, individualActions = false)
        block(minefieldHandler)
    }

    @Test
    fun testOpenArea() {
        handleMinefield { handler ->
            assertTrue(handler.result()[3].isCovered)
            handler.openAt(3, false, openNeighbors = false)
            assertFalse(handler.result()[3].isCovered)
            assertEquals(Mark.None, handler.result()[3].mark)
        }
    }

    @Test
    fun testOpenAreaWithSafeZone() {
        handleMinefield { handler ->
            assertTrue(handler.result()[3].isCovered)
            handler.openAt(3, false, openNeighbors = false)
            assertFalse(handler.result()[3].isCovered)
            assertEquals(Mark.None, handler.result()[3].mark)
        }
    }

    @Test
    fun testRemoveMark() {
        handleMinefield { handler ->
            handler.switchMarkAt(3)

            handler.removeMarkAt(3)
            assertTrue(handler.result()[3].mark == Mark.PurposefulNone)
            assertTrue(handler.result()[3].mark.isNone())
        }
    }

    @Test
    fun testSwitchMarkWithoutQuestionMark() {
        handleMinefield { handler ->
            assertTrue(handler.result()[3].mark.isNone())

            handler.switchMarkAt(3)
            assertTrue(handler.result()[3].mark.isFlag())

            handler.switchMarkAt(3)
            assertTrue(handler.result()[3].mark.isNone())
        }
    }

    @Test
    fun testSwitchMarkWithQuestionMark() {
        handleMinefield(useQuestionMark = true) { handler ->
            assertTrue(handler.result()[3].mark.isNone())

            handler.switchMarkAt(3)
            assertTrue(handler.result()[3].mark.isFlag())

            handler.switchMarkAt(3)
            assertTrue(handler.result()[3].mark.isQuestion())

            handler.switchMarkAt(3)
            assertTrue(handler.result()[3].mark.isNone())
        }
    }

    @Test
    fun testOpenNeighborsClosedArea() {
        handleMinefield { handler ->
            handler.openOrFlagNeighborsOf(3)
            assertEquals(0, handler.result().count { !it.isCovered })
        }
    }

    @Test
    fun testOpenNeighbors() {
        handleMinefield { handler ->
            handler.openAt(5, false, openNeighbors = true)
            handler.openOrFlagNeighborsOf(5)
            assertEquals(1, handler.result().count { !it.isCovered })
        }
    }

    @Test
    fun testOpenNeighborsWithFlags() {
        handleMinefield { handler ->
            handler.openAt(5, false, openNeighbors = true)

            handler.result()
                .filterNeighborsOf(handler.result().first { it.id == 5 })
                .filter { it.hasMine }
                .forEach { handler.switchMarkAt(it.id) }

            handler.openOrFlagNeighborsOf(5)

            val remainCovered =
                handler.result().count { !it.isCovered }
            val remainCoveredNeighbors =
                handler.result().filterNeighborsOf(handler.result().first { it.id == 5 }).count { !it.isCovered }

            assertEquals(9, remainCovered)
            assertEquals(3, remainCoveredNeighbors)
        }
    }

    @Test
    fun testOpenNeighborsWithQuestionMarks() {
        handleMinefield(useQuestionMark = true) { handler ->
            handler.openAt(5, false, openNeighbors = false)

            handler.result()
                .filterNeighborsOf(handler.result().first { it.id == 5 })
                .filter { it.hasMine }
                .forEach {
                    handler.switchMarkAt(it.id)
                    handler.switchMarkAt(it.id)
                }

            handler.openOrFlagNeighborsOf(5)

            val remainCovered =
                handler.result().count { !it.isCovered }
            val remainCoveredNeighbors =
                handler.result().filterNeighborsOf(handler.result().first { it.id == 5 }).count { !it.isCovered }

            assertEquals(1, remainCovered)
            assertEquals(0, remainCoveredNeighbors)
        }
    }
}
