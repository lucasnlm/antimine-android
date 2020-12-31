package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.preferences.models.Minefield
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class MinefieldCreatorTest {
    @Test
    fun testMinefieldCreation() {
        val creator = MinefieldCreator(Minefield(4, 4, 9), Random(200))
        val map = creator.create(2, true)
        assertEquals(
            listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
            map.map { it.id }.toList()
        )
    }

    @Test
    fun testMinefieldCreationMines() {
        val creator = MinefieldCreator(Minefield(5, 5, 99), Random(200))
        val map = creator.create(12, true)
        assertEquals(
            listOf(
                1, 1, 1, 1, 1,
                1, 0, 0, 0, 1,
                1, 0, 0, 0, 1,
                1, 0, 0, 0, 1,
                1, 1, 1, 1, 1
            ),
            map.map { if (it.hasMine) 1 else 0 }.toList()
        )
        assertEquals(16, map.count { it.hasMine })
    }

    @Test
    fun testMinefieldCreationMines2() {
        val creator = MinefieldCreator(Minefield(4, 4, 9), Random(200))
        val map = creator.create(2, true)
        assertEquals(
            listOf(
                1, 0, 0, 0,
                1, 0, 0, 0,
                1, 1, 1, 1,
                1, 0, 1, 1
            ),
            map.map { if (it.hasMine) 1 else 0 }.toList()
        )
    }

    @Test
    fun testMinefieldCreationMines3() {
        val creator = MinefieldCreator(Minefield(4, 4, 9), Random(100))
        val map = creator.create(2, true)
        assertEquals(
            listOf(
                1, 0, 0, 0,
                1, 0, 0, 0,
                1, 0, 1, 1,
                1, 1, 1, 1
            ),
            map.map { if (it.hasMine) 1 else 0 }.toList()
        )
    }

    @Test
    fun testMinefieldCreationMines4() {
        val creator = MinefieldCreator(Minefield(4, 4, 9), Random(50))
        val map = creator.create(2, true)
        assertEquals(
            listOf(
                1, 0, 0, 0,
                1, 0, 0, 0,
                1, 1, 0, 1,
                1, 1, 1, 1
            ),
            map.map { if (it.hasMine) 1 else 0 }.toList()
        )
    }

    @Test
    fun testMinefieldCreationMinesTips() {
        val creator = MinefieldCreator(Minefield(4, 4, 9), Random(200))
        val map = creator.create(2, true)
        assertEquals(
            listOf(
                0, 2, 0, 0,
                0, 5, 3, 2,
                0, 0, 0, 0,
                0, 5, 0, 0
            ),
            map.map { it.minesAround }.toList()
        )
    }

    @Test
    fun testMinefieldCreationMinesTips2() {
        val creator = MinefieldCreator(Minefield(4, 4, 9), Random(100))
        val map = creator.create(2, true)
        assertEquals(
            listOf(
                0, 2, 0, 0,
                0, 4, 2, 2,
                0, 6, 0, 0,
                0, 0, 0, 0
            ),
            map.map { it.minesAround }.toList()
        )
    }

    @Test
    fun testMinefieldCreationMinesTips3() {
        val creator = MinefieldCreator(Minefield(4, 4, 9), Random(50))
        val map = creator.create(2, true)
        assertEquals(
            listOf(
                0, 2, 0, 0,
                0, 4, 2, 1,
                0, 0, 5, 0,
                0, 0, 0, 0
            ),
            map.map { it.minesAround }.toList()
        )
    }

    @Test
    fun testMinefieldCreationPosition() {
        val creator = MinefieldCreator(Minefield(4, 4, 9), Random(200))
        val map = creator.create(2, true)
        assertEquals(
            listOf(
                (0 to 0), (1 to 0), (2 to 0), (3 to 0),
                (0 to 1), (1 to 1), (2 to 1), (3 to 1),
                (0 to 2), (1 to 2), (2 to 2), (3 to 2),
                (0 to 3), (1 to 3), (2 to 3), (3 to 3)
            ),
            map.map { it.posX to it.posY }.toList()
        )
    }

    @Test
    fun testMinefieldMustBeCreatedWithoutMines() {
        val creator = MinefieldCreator(Minefield(4, 4, 9), Random(200))
        val map = creator.createEmpty()
        assertEquals(0, map.count { it.hasMine })
    }
}
