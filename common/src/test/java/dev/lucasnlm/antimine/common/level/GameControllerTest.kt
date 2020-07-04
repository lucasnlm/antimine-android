package dev.lucasnlm.antimine.common.level

import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.Mark
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.common.level.models.Score
import dev.lucasnlm.antimine.core.control.ControlStyle
import dev.lucasnlm.antimine.core.control.GameControl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameControllerTest {

    private fun gameControllerOf(width: Int, height: Int, mines: Int, seed: Long = 0L) =
        GameController(Minefield(width, height, mines), seed)

    private fun GameController.at(id: Int): Area = field.first { it.id == id }

    @Test
    fun aLevelMustBeInitiallyEmpty() {
        gameControllerOf(3, 3, 1).run {
            assertEquals(
                field.toList(),
                listOf(
                    0 to 0, 1 to 0, 2 to 0,
                    0 to 1, 1 to 1, 2 to 1,
                    0 to 2, 1 to 2, 2 to 2
                ).mapIndexed { index, (x, y) ->
                    Area(index, x, y, 0)
                }
            )
        }
    }

    @Test
    fun testGetTile() {
        gameControllerOf(3, 3, 1).run {
            assertEquals(getArea(4), field.first { it.id == 4 })
        }
    }

    @Test
    fun afterOpenATileItsMaskMustBeRemoved() {
        val mineCount = 3
        gameControllerOf(3, 3, mineCount).run {
            plantMinesExcept(3)
            at(3).mark = Mark.Flag
            singleClick(3)
            assertFalse(at(3).mark.isFlag())
            singleClick(3)
            assertTrue(at(3).mark.isNone())
            assertFalse(at(3).isCovered)
        }
    }

    @Test
    fun testPlantMines() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            assertNotEquals(field.filter { it.hasMine }.map { it.id }.first(), 3)
            field.forEach {
                if (it.id == 6) {
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
    fun testGetMinesCount() {
        gameControllerOf(6, 6, 15, 200L).run {
            plantMinesExcept(3)
            assertEquals(15, getMinesCount())
        }
    }

    @Test
    fun testLevelRandomness() {
        assertTrue(
            gameControllerOf(3, 3, 1, 200L).apply {
                plantMinesExcept(3)
            }.at(6).hasMine
        )
        assertTrue(
            gameControllerOf(3, 3, 1, 250L).apply {
                plantMinesExcept(3)
            }.at(1).hasMine
        )
        assertTrue(
            gameControllerOf(3, 3, 1, 100L).apply {
                plantMinesExcept(3)
            }.at(4).hasMine
        )
        assertTrue(
            gameControllerOf(3, 3, 1, 170L).apply {
                plantMinesExcept(3)
            }.at(6).hasMine
        )
    }

    @Test
    fun testMineTips() {
        gameControllerOf(3, 3, 1, 150L).run {
            plantMinesExcept(3)
            assertEquals(
                listOf(
                    1, 0, 1,
                    1, 1, 1,
                    0, 0, 0
                ),
                field.map { it.minesAround }.toList()
            )
        }

        gameControllerOf(3, 3, 2, 200L).run {
            plantMinesExcept(3)
            assertEquals(
                listOf(
                    0, 1, 1,
                    1, 2, 0,
                    0, 2, 1
                ),
                field.map { it.minesAround }.toList()
            )
        }

        gameControllerOf(3, 3, 3, 200L).run {
            plantMinesExcept(3)
            assertEquals(
                listOf(
                    0, 2, 0,
                    1, 3, 0,
                    0, 2, 1
                ),
                field.map { it.minesAround }.toList()
            )
        }

        gameControllerOf(4, 4, 6, 200L).run {
            plantMinesExcept(3)
            assertEquals(
                listOf(
                    0, 0, 2, 1,
                    0, 5, 0, 2,
                    2, 4, 0, 2,
                    0, 2, 1, 1
                ),
                field.map { it.minesAround }.toList()
            )
        }

        gameControllerOf(4, 4, 2, 200L).run {
            plantMinesExcept(3)
            assertEquals(
                listOf(
                    0, 1, 1, 1,
                    0, 2, 0, 2,
                    0, 2, 0, 2,
                    0, 1, 1, 1
                ),
                field.map { it.minesAround }.toList()
            )
        }

        gameControllerOf(4, 4, 1, 200L).run {
            plantMinesExcept(3)
            assertEquals(
                listOf(
                    0, 0, 0, 0,
                    0, 1, 1, 1,
                    0, 1, 0, 1,
                    0, 1, 1, 1
                ),
                field.map { it.minesAround }.toList()
            )
        }

        gameControllerOf(3, 3, 0, 200L).run {
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
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            field.filterNot { it.hasMine }.forEach { it.openTile() }
            runFlagAssistant()
            field.filter { it.hasMine }.map { it.mark.isFlag() }.forEach(::assertTrue)
        }

        gameControllerOf(3, 3, 2, 200L).run {
            plantMinesExcept(3)
            field.filterNot { it.hasMine }.forEach { it.openTile() }
            runFlagAssistant()
            field.filter { it.hasMine }.map { it.mark.isFlag() }.forEach(::assertTrue)
        }

        gameControllerOf(3, 3, 8, 200L).run {
            plantMinesExcept(3)
            field.filterNot { it.hasMine }.forEach { it.openTile() }
            runFlagAssistant()
            field.filter { it.hasMine }.map { it.mark.isFlag() }.forEach(::assertFalse)
        }
    }

    @Test
    fun testSwitchToFlag() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)

            with(getArea(7)) {
                switchMark()
                mark.isFlag()

                field.forEach {
                    if (it.id == 7) {
                        assertTrue(it.mark.isFlag())
                    } else {
                        assertFalse(it.mark.isFlag())
                    }
                }
            }
        }
    }

    @Test
    fun testSwitchToQuestion() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)

            with(getArea(7)) {
                switchMark()
                switchMark()
                assertTrue(mark.isNotNone())
            }

            field.forEach {
                if (it.id == 7) {
                    assertTrue(it.mark.isQuestion())
                } else {
                    assertFalse(it.mark.isQuestion())
                }
            }
        }
    }

    @Test
    fun testSwitchToQuestionWithUseQuestionOff() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            useQuestionMark(false)

            with(getArea(7)) {
                switchMark()
                switchMark()
                assertTrue(mark.isNone())
            }

            field.forEach { assertFalse(it.mark.isQuestion()) }
        }
    }

    @Test
    fun testSwitchBackToEmpty() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)

            with(getArea(7)) {
                switchMark()
                switchMark()
                switchMark()

                assertTrue(mark.isNone())
            }
        }
    }

    @Test
    fun testRemoveMark() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)

            with(getArea(7)) {
                switchMark()
                assertTrue(mark.isFlag())
                removeMark()
                assertTrue(mark.isNone())
            }
        }
    }

    @Test
    fun testTurnOffAllHighlighted() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            getArea(7).highlighted = true
            getArea(8).highlighted = true
            assertEquals(field.count { it.highlighted }, 2)
            turnOffAllHighlighted()
            assertEquals(field.count { it.highlighted }, 0)
        }
    }

    @Test
    fun testOpenField() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            assertEquals(field.filter { it.isCovered }.count(), field.count())
            singleClick(3)
            assertFalse(at(3).isCovered)
        }
    }

    @Test
    fun testOpenNeighborsWithoutFlag() {
        gameControllerOf(5, 5, 24, 200L).run {
            plantMinesExcept(12)
            singleClick(12)
            assertEquals(
                listOf(
                    1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1,
                    1, 1, 0, 1, 1,
                    1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1
                ),
                field.map { if (it.isCovered) 1 else 0 }.toList()
            )

            // It won't open any if the mines were not flagged.
            getArea(12).openNeighbors()
            assertEquals(
                listOf(
                    1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1,
                    1, 1, 0, 1, 1,
                    1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1
                ),
                field.map { if (it.isCovered) 1 else 0 }.toList()
            )
        }
    }

    @Test
    fun testOpenNeighbors() {
        gameControllerOf(5, 5, 15, 200L).run {
            plantMinesExcept(12)
            singleClick(12)
            assertEquals(
                listOf(
                    1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1,
                    1, 1, 0, 1, 1,
                    1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1
                ),
                field.map { if (it.isCovered) 1 else 0 }.toList()
            )

            // It won't open any if the mines were not flagged.
            singleClick(14)
            getArea(14).openNeighbors()
            assertEquals(
                listOf(
                    1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1,
                    1, 1, 0, 1, 0,
                    1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1
                ),
                field.map { if (it.isCovered) 1 else 0 }.toList()
            )

            // After flag its neighbors, it must open all clean neighbors.
            getArea(14).findNeighbors().filter { it.hasMine }.forEach { it.mark = Mark.Flag }
            getArea(14).openNeighbors()
            assertEquals(
                listOf(
                    1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1,
                    1, 1, 0, 0, 0,
                    1, 1, 1, 1, 0,
                    1, 1, 1, 1, 1
                ),
                field.map { if (it.isCovered) 1 else 0 }.toList()
            )
        }
    }

    @Test
    fun testOpenSafeZone() {
        gameControllerOf(3, 3, 1, 0).run {
            plantMinesExcept(3)
            assertEquals(field.filterNot { it.isCovered }.count(), 0)
            singleClick(1)
            assertEquals(field.filterNot { it.isCovered }.count(), 6)
            assertEquals(
                field.filterNot { it.isCovered }.map { it.id }.toList(),
                listOf(0, 1, 2, 3, 4, 5)
            )
        }
    }

    @Test
    fun testShowAllMines() {
        gameControllerOf(3, 3, 5, 200L).run {
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
    fun testFlagAllMines() {
        gameControllerOf(3, 3, 5, 200L).run {
            plantMinesExcept(3)
            field.filter { it.hasMine }.forEach {
                assertFalse(it.mark.isFlag())
            }
            flagAllMines()
            field.filter { it.hasMine }.forEach {
                assertTrue(it.mark.isFlag())
            }
        }
    }

    @Test
    fun testFindExplodedMine() {
        gameControllerOf(3, 3, 5, 200L).run {
            plantMinesExcept(3)
            val mine = field.first { it.hasMine }
            assertEquals(findExplodedMine(), null)
            mine.openTile()
            assertEquals(findExplodedMine(), mine)
        }
    }

    @Test
    fun testTakeExplosionRadius() {
        gameControllerOf(6, 6, 10, 200L).run {
            plantMinesExcept(3)
            val mine = field.last { it.hasMine }
            assertEquals(
                listOf(33, 26, 28, 31, 19, 14, 18, 7, 6, 4),
                takeExplosionRadius(mine).map { it.id }.toList()
            )
        }

        gameControllerOf(6, 6, 10, 200L).run {
            plantMinesExcept(3)
            val mine = field.first { it.hasMine }
            assertEquals(
                listOf(4, 14, 7, 28, 6, 19, 26, 18, 33, 31),
                takeExplosionRadius(mine).map { it.id }.toList()
            )
        }

        gameControllerOf(6, 6, 10, 200L).run {
            plantMinesExcept(3)
            val mine = field.filter { it.hasMine }.elementAt(4)
            assertEquals(
                listOf(18, 19, 6, 7, 14, 26, 31, 33, 28, 4),
                takeExplosionRadius(mine).map { it.id }.toList()
            )
        }
    }

    @Test
    fun testShowWrongFlags() {
        gameControllerOf(3, 3, 5, 200L).run {
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
        gameControllerOf(3, 3, 5, 200L).run {
            plantMinesExcept(3)
            field.filter { it.id != 3 }.map { it.isCovered }.forEach(::assertTrue)
            revealAllEmptyAreas()
            field.filter { it.id != 3 && !it.hasMine }.map { it.isCovered }.forEach(::assertFalse)
            field.filter { it.hasMine }.map { it.isCovered }.forEach(::assertTrue)
        }
    }

    @Test
    fun testGetScore() {
        gameControllerOf(3, 3, 5, 200L).run {
            assertEquals(getScore(), Score(0, 0, 9))
            plantMinesExcept(3)
            assertEquals(getScore(), Score(0, 5, 9))
            field.filter { it.hasMine }.forEach { it.mark = Mark.Flag }
            assertEquals(getScore(), Score(5, 5, 9))
            field.first { it.hasMine }.apply {
                isCovered = false
                mistake = true
            }
            assertEquals(getScore(), Score(4, 5, 9))
        }
    }

    @Test
    fun testFlaggedAllMines() {
        gameControllerOf(3, 3, 5, 200L).run {
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
        gameControllerOf(3, 3, 5, 200L).run {
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
        val width = 12
        val height = 12
        val mines = 9
        gameControllerOf(width, height, mines, 150L).run {
            plantMinesExcept(3)
            field.filterNot { it.hasMine }.take(width * height - 1 - mines).forEach {
                singleClick(it.id)
                assertFalse(hasIsolatedAllMines())
                assertFalse(isGameOver())
            }

            field.first { !it.hasMine && it.isCovered }.run {
                singleClick(id)
                assertTrue(hasIsolatedAllMines())
                assertTrue(isGameOver())
            }
        }
    }

    @Test
    fun testHasAnyMineExploded() {
        gameControllerOf(3, 3, 5, 200L).run {
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
    fun testGameOverWithMineExploded() {
        gameControllerOf(3, 3, 5, 200L).run {
            plantMinesExcept(3)
            assertFalse(isGameOver())

            field.first { it.hasMine }.apply {
                isCovered = false
                mistake = true
            }

            assertTrue(isGameOver())
        }
    }

    @Test
    fun testVictory() {
        gameControllerOf(3, 3, 5, 200L).run {
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
        gameControllerOf(3, 3, 0, 200L).run {
            plantMinesExcept(3)
            assertFalse(checkVictory())
        }
    }

    @Test
    fun testControlFirstActionWithStandard() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            updateGameControl(GameControl.fromControlType(ControlStyle.Standard))
            assertTrue(at(3).isCovered)
            singleClick(3)
            assertFalse(at(3).isCovered)
        }
    }

    @Test
    fun testControlSecondActionWithStandard() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            updateGameControl(GameControl.fromControlType(ControlStyle.Standard))

            useQuestionMark(true)
            longPress(3)
            assertTrue(at(3).mark.isFlag())
            assertTrue(at(3).isCovered)
            longPress(3)
            assertTrue(at(3).mark.isQuestion())
            assertTrue(at(3).isCovered)
            longPress(3)
            assertTrue(at(3).mark.isNone())
            assertTrue(at(3).isCovered)

            useQuestionMark(false)
            longPress(3)
            assertTrue(at(3).mark.isFlag())
            assertTrue(at(3).isCovered)
            longPress(3)
            assertTrue(at(3).mark.isNone())
            assertTrue(at(3).isCovered)
        }
    }

    @Test
    fun testControlStandardOpenMultiple() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            updateGameControl(GameControl.fromControlType(ControlStyle.Standard))
            singleClick(3)
            assertFalse(at(3).isCovered)
            at(3).findNeighbors().forEach {
                assertTrue(it.isCovered)
            }

            field.filter { it.hasMine }.forEach {
                longPress(it.id)
                assertTrue(it.mark.isFlag())
            }

            longPress(3)
            at(3).findNeighbors().forEach {
                if (it.hasMine) {
                    assertTrue(it.isCovered)
                } else {
                    assertFalse(it.isCovered)
                }
            }
        }
    }

    @Test
    fun testControlFirstActionWithFastFlag() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            updateGameControl(GameControl.fromControlType(ControlStyle.FastFlag))
            singleClick(3)
            assertTrue(at(3).isCovered)
            assertTrue(at(3).mark.isFlag())
            longPress(3)
            assertFalse(at(3).mark.isFlag())
            assertTrue(at(3).isCovered)
            longPress(3)
            assertFalse(at(3).isCovered)
        }
    }

    @Test
    fun testControlFastFlagOpenMultiple() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            updateGameControl(GameControl.fromControlType(ControlStyle.FastFlag))
            longPress(3)
            assertFalse(at(3).isCovered)
            at(3).findNeighbors().forEach {
                assertTrue(it.isCovered)
            }

            field.filter { it.hasMine }.forEach {
                singleClick(it.id)
                assertTrue(it.mark.isFlag())
            }

            singleClick(3)
            at(3).findNeighbors().forEach {
                if (it.hasMine) {
                    assertTrue(it.isCovered)
                } else {
                    assertFalse(it.isCovered)
                }
            }
        }
    }

    @Test
    fun testControlFirstActionWithDoubleClick() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            updateGameControl(GameControl.fromControlType(ControlStyle.DoubleClick))
            singleClick(3)
            assertTrue(at(3).isCovered)
            assertTrue(at(3).mark.isFlag())
            doubleClick(3)
            assertFalse(at(3).mark.isFlag())
            assertTrue(at(3).isCovered)
            doubleClick(3)
            assertFalse(at(3).isCovered)
        }
    }

    @Test
    fun testControlFirstActionWithDoubleClickAndWithoutQuestionMark() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            updateGameControl(GameControl.fromControlType(ControlStyle.DoubleClick))

            useQuestionMark(true)
            var targetId = 4
            singleClick(targetId)
            assertTrue(at(targetId).isCovered)
            assertTrue(at(targetId).mark.isFlag())
            singleClick(targetId)
            assertTrue(at(targetId).mark.isQuestion())
            assertTrue(at(targetId).isCovered)
            singleClick(targetId)
            assertTrue(at(targetId).mark.isNone())
            assertTrue(at(targetId).isCovered)
            doubleClick(targetId)
            assertFalse(at(targetId).isCovered)

            useQuestionMark(false)
            targetId = 3
            singleClick(targetId)
            assertTrue(at(targetId).isCovered)
            assertTrue(at(targetId).mark.isFlag())
            singleClick(targetId)
            assertFalse(at(targetId).mark.isFlag())
            assertTrue(at(targetId).isCovered)
            doubleClick(targetId)
            assertFalse(at(targetId).isCovered)
        }
    }

    @Test
    fun testControlDoubleClickOpenMultiple() {
        gameControllerOf(3, 3, 1, 200L).run {
            plantMinesExcept(3)
            updateGameControl(GameControl.fromControlType(ControlStyle.DoubleClick))
            doubleClick(3)
            assertFalse(at(3).isCovered)
            at(3).findNeighbors().forEach {
                assertTrue(it.isCovered)
            }

            field.filter { it.hasMine }.forEach {
                singleClick(it.id)
                assertTrue(it.mark.isFlag())
            }

            doubleClick(3)
            at(3).findNeighbors().forEach {
                if (it.hasMine) {
                    assertTrue(it.isCovered)
                } else {
                    assertFalse(it.isCovered)
                }
            }
        }
    }

    @Test
    fun testIfDoubleClickPlantMinesOnFirstClick() {
        gameControllerOf(9, 9, 72, 200L).run {
            updateGameControl(GameControl.fromControlType(ControlStyle.DoubleClick))
            assertFalse(hasMines)
            assertEquals(0, field.filterNot { it.isCovered }.count())
            singleClick(40)
            assertTrue(hasMines)
            at(40).findNeighbors().forEach { assertFalse(it.isCovered) }
        }
    }

    @Test
    fun testIfFastFlagPlantMinesOnFirstClick() {
        gameControllerOf(9, 9, 72, 200L).run {
            updateGameControl(GameControl.fromControlType(ControlStyle.FastFlag))
            assertFalse(hasMines)
            assertEquals(0, field.filterNot { it.isCovered }.count())
            singleClick(40)
            assertTrue(hasMines)
            at(40).findNeighbors().forEach { assertFalse(it.isCovered) }
        }
    }
}
