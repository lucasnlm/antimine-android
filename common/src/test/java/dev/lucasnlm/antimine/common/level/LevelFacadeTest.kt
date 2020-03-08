package dev.lucasnlm.antimine.common.level

import dev.lucasnlm.antimine.common.level.data.Area
import dev.lucasnlm.antimine.common.level.data.LevelSetup
import dev.lucasnlm.antimine.common.level.data.Mark
import dev.lucasnlm.antimine.common.level.data.isFlag
import dev.lucasnlm.antimine.common.level.data.isQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelFacadeTest {

    private fun levelFacadeOf(width: Int, height: Int, mines: Int, seed: Long = 0L) =
        LevelFacade(0, LevelSetup(width, height, mines), seed)

    private fun LevelFacade.at(id: Int): Area = field.first { it.id == id }

    @Test
    fun testLevelEmptyBuilding() {
        levelFacadeOf(3, 3, 1).run {
            assertEquals(
                field.toList(),
                listOf(
                    Area(0, 0, 0, 0),
                    Area(1, 1, 0, 0),
                    Area(2, 2, 0, 0),
                    Area(3, 0, 1, 0),
                    Area(4, 1, 1, 0),
                    Area(5, 2, 1, 0),
                    Area(6, 0, 2, 0),
                    Area(7, 1, 2, 0),
                    Area(8, 2, 2, 0)
                )
            )
        }
    }

    @Test
    fun testDismissFlagAfterOpen() {
        val mineCount = 3
        levelFacadeOf(3, 3, mineCount).run {
            plantMinesExcept(3)
            clickArea(3)
            field.filter { it.isCovered }.forEach { it.mark = Mark.Flag }
            field.filterNot { it.hasMine }.map { it.id }.forEach { clickArea(it) }
            assertEquals(
                field.filter { it.isCovered }.count { it.mark.isFlag() },
                mineCount
            )
        }
    }

    @Test
    fun testPlantMines() {
        levelFacadeOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            assertNotEquals(field.filter { it.hasMine }.map { it.id }.first(), 3)
            field.forEach {
                if (it.id == 7) {
                    assertTrue(it.hasMine)
                } else {
                    assertFalse(it.hasMine)
                }
            }
            assertTrue(hasMines)
            assertEquals(remainingMines(), 1)
        }
    }

    @Test
    fun testPlantMinesWithSafeArea() {
        levelFacadeOf(9, 9, 12, 200L).run {
            plantMinesExcept(3, true)
            field.filter { it.safeZone }.forEach {
                assertFalse(it.hasMine)
            }
        }
    }

    @Test
    fun testLevelRandomness() {
        assertTrue(
            levelFacadeOf(3, 3, 1, 200L).apply {
                plantMinesExcept(3)
            }.at(7).hasMine
        )
        assertTrue(
            levelFacadeOf(3, 3, 1, 250L).apply {
                plantMinesExcept(3)
            }.at(0).hasMine
        )
        assertTrue(
            levelFacadeOf(3, 3, 1, 100L).apply {
                plantMinesExcept(3)
            }.at(8).hasMine
        )
        assertTrue(
            levelFacadeOf(3, 3, 1, 170L).apply {
                plantMinesExcept(3)
                println(field)
            }.at(2).hasMine
        )
    }

    @Test
    fun testMineTips() {
        levelFacadeOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            assertEquals(
                listOf(
                    0, 0, 0,
                    1, 1, 1,
                    1, 0, 1
                ),
                field.map { it.minesAround }.toList()
            )
        }

        levelFacadeOf(3, 3, 2, 200L).run {
            plantMinesExcept(3)
            assertEquals(
                listOf(
                    1, 0, 1,
                    2, 2, 2,
                    1, 0, 1
                ),
                field.map { it.minesAround }.toList()
            )
        }

        levelFacadeOf(3, 3, 3, 200L).run {
            plantMinesExcept(3)
            assertEquals(
                listOf(
                    0, 0, 1,
                    3, 3, 2,
                    1, 0, 1
                ),
                field.map { it.minesAround }.toList()
            )
        }

        levelFacadeOf(4, 4, 6, 200L).run {
            plantMinesExcept(3)
            assertEquals(
                listOf(
                    0, 0, 0, 1,
                    3, 0, 3, 1,
                    2, 3, 2, 1,
                    0, 2, 0, 1
                ),
                field.map { it.minesAround }.toList()
            )
        }

        levelFacadeOf(4, 4, 2, 200L).run {
            plantMinesExcept(3)
            assertEquals(
                listOf(
                    0, 0, 1, 0,
                    2, 2, 1, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0
                ),
                field.map { it.minesAround }.toList()
            )
        }

        levelFacadeOf(4, 4, 1, 200L).run {
            plantMinesExcept(3)
            assertEquals(
                listOf(
                    1, 0, 1, 0,
                    1, 1, 1, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0
                ),
                field.map { it.minesAround }.toList()
            )
        }

        levelFacadeOf(3, 3, 0, 200L).run {
            plantMinesExcept(3)
            assertEquals(
                listOf(
                    0, 0, 0,
                    0, 0, 0,
                    0, 0, 0
                ),
                field.map { it.minesAround }.toList()
            )
        }
    }

    @Test
    fun testFlagAssistant() {
        levelFacadeOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            field.filterNot { it.hasMine }.forEach { openField(it) }
            runFlagAssistant()
            field.filter { it.hasMine }.map { it.mark.isFlag() }.forEach(::assertTrue)
        }

        levelFacadeOf(3, 3, 2, 200L).run {
            plantMinesExcept(3)
            field.filterNot { it.hasMine }.forEach { openField(it) }
            runFlagAssistant()
            field.filter { it.hasMine }.map { it.mark.isFlag() }.forEach(::assertTrue)
        }

        levelFacadeOf(3, 3, 8, 200L).run {
            plantMinesExcept(3)
            field.filterNot { it.hasMine }.forEach { openField(it) }
            runFlagAssistant()
            field.filter { it.hasMine }.map { it.mark.isFlag() }.forEach(::assertFalse)
        }
    }

    @Test
    fun testSwitchToFlag() {
        levelFacadeOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            switchMarkAt(7)
            field.forEach {
                if (it.id == 7) {
                    assertTrue(it.mark.isFlag())
                } else {
                    assertFalse(it.mark.isFlag())
                }
            }
            assertTrue(hasMarkOn(7))
        }
    }

    @Test
    fun testSwitchToQuestion() {
        levelFacadeOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            switchMarkAt(7)
            switchMarkAt(7)
            field.forEach {
                if (it.id == 7) {
                    assertTrue(it.mark.isQuestion())
                } else {
                    assertFalse(it.mark.isQuestion())
                }
            }
            assertTrue(hasMarkOn(7))
        }
    }

    @Test
    fun testSwitchBackToEmpty() {
        levelFacadeOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            switchMarkAt(7)
            switchMarkAt(7)
            switchMarkAt(7)
            assertFalse(hasMarkOn(7))
        }
    }

    @Test
    fun testOpenField() {
        levelFacadeOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            assertEquals(field.filter { it.isCovered }.count(), field.count())
            clickArea(3)
            assertFalse(at(3).isCovered)
        }
    }

    @Test
    fun testOpenNeighbors() {
        levelFacadeOf(5, 5, 24, 200L).run {
            plantMinesExcept(12)
            clickArea(12)
            assertEquals(
                listOf(
                    1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1,
                    1, 1, 0, 1, 1,
                    1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1),
                field.map { if (it.isCovered) 1 else 0 }.toList()
            )
            openNeighbors(12)
            assertEquals(
                listOf(
                    1, 1, 1, 1, 1,
                    1, 0, 0, 0, 1,
                    1, 0, 0, 0, 1,
                    1, 0, 0, 0, 1,
                    1, 1, 1, 1, 1),
                field.map { if (it.isCovered) 1 else 0 }.toList()
            )
        }
    }

    @Test
    fun testOpenSafeZone() {
        levelFacadeOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            assertEquals(field.filter { it.isCovered }.count(), field.count())
            clickArea(1)
            assertEquals(field.filter { it.isCovered }.count(), field.count() - 6)
            assertEquals(
                field.filterNot { it.isCovered }.map { it.id }.toList(),
                listOf(0, 1, 2, 3, 4, 5)
            )
        }
    }

    @Test
    fun testShowAllMines() {
        levelFacadeOf(3, 3, 5, 200L).run {
            plantMinesExcept(3)
            showAllMines()
            field.filter { it.hasMine && it.mistake }.forEach {
                assertEquals(it.isCovered, false)
            }
            field.filter { it.hasMine && it.mark.isFlag() }.forEach {
                assertEquals(it.isCovered, true)
            }
        }
    }

    @Test
    fun testShowWrongFlags() {
        levelFacadeOf(3, 3, 5, 200L).run {
            plantMinesExcept(3)
            val wrongFlag = field.first { !it.hasMine }.apply {
                mark = Mark.Flag
            }
            val rightFlag = field.first { it.hasMine }.apply {
                mark = Mark.Flag
            }
            showWrongFlags()
            assertTrue(wrongFlag.mistake)
            assertFalse(rightFlag.mistake)
        }
    }

    @Test
    fun testRevealAllEmptyAreas() {
        levelFacadeOf(3, 3, 5, 200L).run {
            plantMinesExcept(3)
            field.filter { it.id != 3 }.map { it.isCovered }.forEach(::assertTrue)
            revealAllEmptyAreas()
            field.filter { it.id != 3 && !it.hasMine }.map { it.isCovered }.forEach(::assertFalse)
            field.filter { it.hasMine }.map { it.isCovered }.forEach(::assertTrue)
        }
    }

    @Test
    fun testFlaggedAllMines() {
        levelFacadeOf(3, 3, 5, 200L).run {
            plantMinesExcept(3)
            assertFalse(hasFlaggedAllMines())
            field.forEach {
                if (it.hasMine) {
                    it.mark = Mark.Flag
                }
            }
            assertTrue(hasFlaggedAllMines())
        }
    }

    @Test
    fun testRemainingMines() {
        levelFacadeOf(3, 3, 5, 200L).run {
            plantMinesExcept(3)
            assertEquals(remainingMines(), 5)

            field.filter { it.hasMine }.take(2).forEach {
                it.mark = Mark.Flag
            }
            assertEquals(remainingMines(), 3)

            field.filter { it.hasMine }.forEach {
                it.mark = Mark.Flag
            }
            assertEquals(remainingMines(), 0)
        }
    }

    @Test
    fun testHasIsolatedAllMines() {
        levelFacadeOf(3, 3, 5, 200L).run {
            plantMinesExcept(3)
            assertFalse(hasIsolatedAllMines())

            clickArea(6)
            assertFalse(hasIsolatedAllMines())

            clickArea(4)
            assertFalse(hasIsolatedAllMines())

            clickArea(2)
            assertFalse(hasIsolatedAllMines())

            clickArea(3)
            assertFalse(hasIsolatedAllMines())

            clickArea(5)
            assertFalse(hasIsolatedAllMines())

            clickArea(8)
            assertTrue(hasIsolatedAllMines())
        }
    }

    @Test
    fun testHasAnyMineExploded() {
        levelFacadeOf(3, 3, 5, 200L).run {
            plantMinesExcept(3)
            assertFalse(hasAnyMineExploded())

            field.first { it.hasMine }.apply {
                isCovered = false
                mistake = true
            }
            assertTrue(hasAnyMineExploded())
        }
    }

    @Test
    fun testVictory() {
        levelFacadeOf(3, 3, 5, 200L).run {
            plantMinesExcept(3)
            assertFalse(checkVictory())

            field.filter { it.hasMine }.forEach { it.mark = Mark.Flag }
            assertFalse(checkVictory())

            field.filterNot { it.hasMine }.forEach { it.isCovered = false }
            assertTrue(checkVictory())

            field.first { it.hasMine }.mistake = true
            assertFalse(checkVictory())
        }
    }

    @Test
    fun testCantShowVictoryIfHasNoMines() {
        levelFacadeOf(3, 3, 0, 200L).run {
            plantMinesExcept(3)
            assertFalse(checkVictory())
        }
    }
}
