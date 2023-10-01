package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MinefieldCreatorTest {
    @Test
    fun testMinefieldCreation() =
        runTest {
            val creator = MinefieldCreatorImpl(Minefield(4, 4, 9), 200)
            val map = creator.create(2)
            assertEquals(
                listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
                map.map { it.id }.toList(),
            )
        }

    @Test
    fun testMinefieldCreationMines() =
        runTest {
            val creator = MinefieldCreatorImpl(Minefield(5, 5, 99), 200)
            val map = creator.create(12)
            assertEquals(
                listOf(
                    1, 1, 1, 1, 1,
                    1, 0, 0, 0, 1,
                    1, 0, 0, 0, 1,
                    1, 0, 0, 0, 1,
                    1, 1, 1, 1, 1,
                ),
                map.map { if (it.hasMine) 1 else 0 }.toList(),
            )
            assertEquals(16, map.count { it.hasMine })
        }

    @Test
    fun testMinefieldCreationMines2() =
        runTest {
            val creator = MinefieldCreatorImpl(Minefield(4, 4, 9), 200)
            val map = creator.create(2)
            assertEquals(
                listOf(
                    1, 0, 0, 0,
                    1, 0, 0, 0,
                    1, 1, 1, 1,
                    1, 1, 1, 0,
                ),
                map.map { if (it.hasMine) 1 else 0 }.toList(),
            )
        }

    @Test
    fun testMinefieldCreationMines3() =
        runTest {
            val creator = MinefieldCreatorImpl(Minefield(4, 4, 9), 100)
            val map = creator.create(2)
            assertEquals(
                listOf(
                    1, 0, 0, 0,
                    1, 0, 0, 0,
                    1, 1, 1, 0,
                    1, 1, 1, 1,
                ),
                map.map { if (it.hasMine) 1 else 0 }.toList(),
            )
        }

    @Test
    fun testMinefieldCreationMines4() =
        runTest {
            val creator = MinefieldCreatorImpl(Minefield(4, 4, 9), 50)
            val map = creator.create(2)
            assertEquals(
                listOf(
                    1, 0, 0, 0,
                    1, 0, 0, 0,
                    1, 1, 1, 1,
                    1, 0, 1, 1,
                ),
                map.map { if (it.hasMine) 1 else 0 }.toList(),
            )
        }

    @Test
    fun testMinefieldCreationMinesTips() =
        runTest {
            val creator = MinefieldCreatorImpl(Minefield(4, 4, 9), 200)
            val map = creator.create(2)
            assertEquals(
                listOf(
                    0, 2, 0, 0,
                    0, 5, 3, 2,
                    0, 0, 0, 0,
                    0, 0, 0, 3,
                ),
                map.map { it.minesAround }.toList(),
            )
        }

    @Test
    fun testMinefieldCreationMinesTips2() =
        runTest {
            val creator = MinefieldCreatorImpl(Minefield(4, 4, 9), 100)
            val map = creator.create(2)
            assertEquals(
                listOf(
                    0, 2, 0, 0,
                    0, 5, 2, 1,
                    0, 0, 0, 3,
                    0, 0, 0, 0,
                ),
                map.map { it.minesAround }.toList(),
            )
        }

    @Test
    fun testMinefieldCreationMinesTips3() =
        runTest {
            val creator = MinefieldCreatorImpl(Minefield(4, 4, 9), 50)
            val map = creator.create(2)
            assertEquals(
                listOf(
                    0, 2, 0, 0,
                    0, 5, 3, 2,
                    0, 0, 0, 0,
                    0, 5, 0, 0,
                ),
                map.map { it.minesAround }.toList(),
            )
        }

    @Test
    fun testMinefieldCreationPosition() =
        runTest {
            val creator = MinefieldCreatorImpl(Minefield(4, 4, 9), 200)
            val map = creator.create(2)
            assertEquals(
                listOf(
                    (0 to 0), (1 to 0), (2 to 0), (3 to 0),
                    (0 to 1), (1 to 1), (2 to 1), (3 to 1),
                    (0 to 2), (1 to 2), (2 to 2), (3 to 2),
                    (0 to 3), (1 to 3), (2 to 3), (3 to 3),
                ),
                map.map { it.posX to it.posY }.toList(),
            )
        }

    @Test
    fun testMinefieldMustBeCreatedWithoutMines() =
        runTest {
            val creator = MinefieldCreatorImpl(Minefield(4, 4, 9), 200)
            val map = creator.createEmpty()
            assertEquals(0, map.count { it.hasMine })
        }
}
