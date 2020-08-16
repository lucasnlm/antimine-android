package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.GameController
import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.Mark
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.common.level.models.Score
import dev.lucasnlm.antimine.core.control.ControlStyle
import dev.lucasnlm.antimine.core.control.GameControl
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameControllerTest {
    private fun withGameController(
        clickOnCreate: Boolean = true,
        block: (GameController) -> Unit
    ) {
        val minefield = Minefield(10, 10, 20)
        val gameController = GameController(minefield, 200L)
        if (clickOnCreate) {
            gameController.fakeSingleClick(10)
        }
        block(gameController)
    }

    @Test
    fun testGetMinesCount() = runBlockingTest {
        withGameController {
            assertEquals(20, it.getMinesCount())
        }
    }

    @Test
    fun testGetScore() = runBlockingTest {
        withGameController { controller ->
            assertEquals(Score(0, 20, 100), controller.getScore())

            repeat(20) { right ->
                controller.field.filter { it.hasMine }.take(right).forEach { it.mark = Mark.Flag }
                assertEquals(Score(right, 20, 100), controller.getScore())
            }
        }
    }

    @Test
    fun testGetScoreWithQuestion() = runBlockingTest {
        withGameController { controller ->
            assertEquals(Score(0, 20, 100), controller.getScore())

            controller.field.filter { it.hasMine }.take(5).forEach { it.mark = Mark.Question }
            assertEquals(Score(0, 20, 100), controller.getScore())
        }
    }

    @Test
    fun testFlagAllMines() = runBlockingTest {
        withGameController { controller ->
            controller.flagAllMines()
            val actual = controller.field.filter { it.hasMine }.count { it.isCovered && it.mark.isFlag() }
            assertEquals(20, actual)
        }
    }

    @Test
    fun testFindExplodedMine() = runBlockingTest {
        withGameController { controller ->
            assertNull(controller.findExplodedMine())
            val target = controller.field.first { it.hasMine }
            launch {
                controller.singleClick(target.id).collect { it.second.collect() }
            }
            assertEquals(target.id, controller.findExplodedMine()?.id ?: - 1)
        }
    }

    @Test
    fun testTakeExplosionRadius() = runBlockingTest {
        withGameController { controller ->
            val lastMine = controller.field.last { it.hasMine }
            assertEquals(
                listOf(95, 85, 74, 73, 65, 88, 55, 91, 45, 52, 90, 47, 59, 42, 36, 32, 39, 28, 4, 3),
                controller.takeExplosionRadius(lastMine).map { it.id }.toList()
            )

            val firstMine = controller.field.first { it.hasMine }
            assertEquals(
                listOf(3, 4, 32, 42, 36, 45, 52, 28, 55, 47, 65, 39, 73, 74, 59, 85, 91, 95, 88, 90),
                controller.takeExplosionRadius(firstMine).map { it.id }.toList()
            )

            val midMine = controller.field.filter { it.hasMine }.take(controller.getMinesCount() / 2).last()
            assertEquals(
                listOf(52, 42, 32, 73, 74, 55, 45, 65, 91, 85, 36, 90, 95, 3, 47, 4, 28, 88, 59, 39),
                controller.takeExplosionRadius(midMine).map { it.id }.toList()
            )
        }
    }

    @Test
    fun testShowAllMines() = runBlockingTest {
        withGameController { controller ->
            controller.showAllMines()
            controller.field.filter { it.hasMine && it.mistake }.forEach {
                assertEquals(it.isCovered, false)
            }
            controller.field.filter { it.hasMine && it.mark.isFlag() }.forEach {
                assertEquals(it.isCovered, true)
            }
        }
    }

    @Test
    fun testShowWrongFlags() = runBlockingTest {
        withGameController { controller ->
            val wrongFlag = controller.field.first { !it.hasMine }.apply {
                mark = Mark.Flag
            }
            val rightFlag = controller.field.first { it.hasMine }.apply {
                mark = Mark.Flag
            }
            controller.showWrongFlags()
            assertTrue(wrongFlag.mistake)
            assertFalse(rightFlag.mistake)
        }
    }

    @Test
    fun testRevealAllEmptyAreas() = runBlockingTest {
        withGameController { controller ->
            val covered = controller.field.filter { it.isCovered }
            assertTrue(covered.isNotEmpty())
            controller.revealAllEmptyAreas()
            assertEquals(controller.field.filter { it.hasMine }, controller.field.filter { it.isCovered })
        }
    }

    @Test
    fun testFlaggedAllMines() = runBlockingTest {
        withGameController { controller ->
            assertFalse(controller.hasFlaggedAllMines())
            controller.field.filter { it.hasMine }.take(10).forEach { it.mark = Mark.Flag }
            assertFalse(controller.hasFlaggedAllMines())
            controller.field.filter { it.hasMine }.forEach { it.mark = Mark.Flag }
            assertTrue(controller.hasFlaggedAllMines())
        }
    }

    @Test
    fun testRemainingMines() = runBlockingTest {
        withGameController { controller ->
            assertEquals(20, controller.remainingMines())

            repeat(20) { flagCount ->
                controller.field.filter { it.hasMine }.take(flagCount).forEach { it.mark = Mark.Flag }
                assertEquals("flagging $flagCount mines", 20 - flagCount, controller.remainingMines())
            }
        }
    }

    @Test
    fun testHasIsolatedAllMines() = runBlockingTest {
        withGameController { controller ->
            assertFalse(controller.hasIsolatedAllMines())
            assertFalse(controller.isGameOver())

            controller.field.filter { !it.hasMine }.forEach {
                it.isCovered = false
            }

            assertTrue(controller.hasIsolatedAllMines())
            assertTrue(controller.isGameOver())
        }
    }

    @Test
    fun testHasAnyMineExploded() = runBlockingTest {
        withGameController { controller ->
            assertFalse(controller.hasAnyMineExploded())

            controller.field.first { it.hasMine }.also {
                it.isCovered = false
                it.mistake = true
            }

            assertTrue(controller.hasAnyMineExploded())
        }
    }

    @Test
    fun testGameOverWithMineExploded() = runBlockingTest {
        withGameController { controller ->
            assertFalse(controller.isGameOver())

            controller.field.first { it.hasMine }.also {
                it.isCovered = false
                it.mistake = true
            }

            assertTrue(controller.isGameOver())
        }
    }

    @Test
    fun testVictory() = runBlockingTest {
        withGameController { controller ->
            assertFalse(controller.checkVictory())

            controller.field.filter { it.hasMine }.forEach { it.mark = Mark.Flag }
            assertFalse(controller.checkVictory())

            controller.field.filterNot { it.hasMine }.forEach { it.isCovered = false }
            assertTrue(controller.checkVictory())

            controller.field.first { it.hasMine }.mistake = true
            assertFalse(controller.checkVictory())
        }
    }

    @Test
    fun testCantShowVictoryIfHasNoMines() = runBlockingTest {
        withGameController { controller ->
            assertFalse(controller.checkVictory())
        }
    }

    @Test
    fun testControlFirstActionWithStandard() = runBlockingTest {
        withGameController { controller ->
            controller.updateGameControl(GameControl.fromControlType(ControlStyle.Standard))
            assertTrue(controller.at(3).isCovered)
            controller.fakeSingleClick(3)
            assertFalse(controller.at(3).isCovered)
        }
    }

    @Test
    fun testControlSecondActionWithStandard() = runBlockingTest {
        withGameController { controller ->
            controller.run {
                updateGameControl(GameControl.fromControlType(ControlStyle.Standard))
                useQuestionMark(true)

                fakeLongPress(4)
                assertTrue(at(4).mark.isFlag())
                assertTrue(at(4).isCovered)
                fakeLongPress(4)
                assertTrue(at(4).mark.isQuestion())
                assertTrue(at(4).isCovered)
                fakeLongPress(4)
                assertTrue(at(4).mark.isNone())
                assertTrue(at(4).isCovered)

                useQuestionMark(false)
                fakeLongPress(4)
                assertTrue(at(4).mark.isFlag())
                assertTrue(at(4).isCovered)
                fakeLongPress(4)
                assertTrue(at(4).mark.isNone())
                assertTrue(at(4).isCovered)
            }
        }
    }

    @Test
    fun testControlStandardOpenMultiple() = runBlockingTest {
        withGameController { controller ->
            controller.run {
                updateGameControl(GameControl.fromControlType(ControlStyle.Standard))
                fakeSingleClick(14)
                assertFalse(at(14).isCovered)
                field.filterNeighborsOf(at(14)).forEach {
                    assertTrue(it.isCovered)
                }

                field.filter { it.hasMine }.forEach {
                    fakeLongPress(it.id)
                    assertTrue(it.mark.isFlag())
                }

                fakeLongPress(14)
                field.filterNeighborsOf(at(14)).forEach {
                    if (it.hasMine) {
                        assertTrue(it.isCovered)
                    } else {
                        assertFalse(it.isCovered)
                    }
                }
            }
        }
    }

    @Test
    fun testControlFirstActionWithFastFlag() {
        withGameController { controller ->
            controller.run {
                updateGameControl(GameControl.fromControlType(ControlStyle.FastFlag))
                fakeSingleClick(3)
                assertTrue(at(3).isCovered)
                assertTrue(at(3).mark.isFlag())
                fakeLongPress(3)
                assertFalse(at(3).mark.isFlag())
                assertTrue(at(3).isCovered)
                fakeLongPress(3)
                assertFalse(at(3).isCovered)
            }
        }
    }

    @Test
    fun testControlFirstActionWithInvertedDoubleClick() {
        withGameController { controller ->
            controller.run {
                updateGameControl(GameControl.fromControlType(ControlStyle.DoubleClickInverted))
                assertTrue(at(3).isCovered)
                fakeDoubleClick(3)
                assertTrue(at(3).isCovered)
                assertTrue(at(3).mark.isFlag())
                fakeDoubleClick(3)
                assertFalse(at(3).mark.isFlag())
                assertTrue(at(3).isCovered)
            }
        }
    }

    @Test
    fun testControlSecondActionWithInvertedDoubleClick() {
        withGameController { controller ->
            controller.run {
                updateGameControl(GameControl.fromControlType(ControlStyle.DoubleClickInverted))
                assertTrue(at(3).isCovered)
                fakeSingleClick(3)
                assertFalse(at(3).isCovered)
            }
        }
    }

    @Test
    fun testControlFastFlagOpenMultiple() {
        withGameController { controller ->
            controller.run {
                updateGameControl(GameControl.fromControlType(ControlStyle.FastFlag))
                fakeLongPress(14)
                assertFalse(at(14).isCovered)
                field.filterNeighborsOf(at(14)).forEach {
                    assertTrue(it.isCovered)
                }

                field.filter { it.hasMine }.forEach {
                    fakeSingleClick(it.id)
                    assertTrue(it.mark.isFlag())
                }

                fakeSingleClick(14)
                field.filterNeighborsOf(at(14)).forEach {
                    if (it.hasMine) {
                        assertTrue(it.isCovered)
                    } else {
                        assertFalse(it.isCovered)
                    }
                }
            }
        }
    }

    @Test
    fun testControlFirstActionWithDoubleClick() {
        withGameController { controller ->
            controller.run {
                updateGameControl(GameControl.fromControlType(ControlStyle.DoubleClick))
                fakeSingleClick(3)
                assertTrue(at(3).isCovered)
                assertTrue(at(3).mark.isFlag())
                fakeDoubleClick(3)
                assertFalse(at(3).mark.isFlag())
                assertTrue(at(3).isCovered)
                fakeDoubleClick(3)
                assertFalse(at(3).isCovered)
            }
        }
    }

    @Test
    fun testControlFirstActionWithDoubleClickAndWithoutQuestionMark() {
        withGameController { controller ->
            controller.run {
                updateGameControl(GameControl.fromControlType(ControlStyle.DoubleClick))

                useQuestionMark(true)
                var targetId = 4
                fakeSingleClick(targetId)
                assertTrue(at(targetId).isCovered)
                assertTrue(at(targetId).mark.isFlag())
                fakeSingleClick(targetId)
                assertTrue(at(targetId).mark.isQuestion())
                assertTrue(at(targetId).isCovered)
                fakeSingleClick(targetId)
                assertTrue(at(targetId).mark.isNone())
                assertTrue(at(targetId).isCovered)
                fakeDoubleClick(targetId)
                assertFalse(at(targetId).isCovered)

                useQuestionMark(false)
                targetId = 3
                fakeSingleClick(targetId)
                assertTrue(at(targetId).isCovered)
                assertTrue(at(targetId).mark.isFlag())
                fakeSingleClick(targetId)
                assertFalse(at(targetId).mark.isFlag())
                assertTrue(at(targetId).isCovered)
                fakeDoubleClick(targetId)
                assertFalse(at(targetId).isCovered)
            }
        }
    }

    @Test
    fun testControlDoubleClickOpenMultiple() {
        withGameController { controller ->
            controller.run {
                updateGameControl(GameControl.fromControlType(ControlStyle.DoubleClick))
                fakeDoubleClick(14)
                assertFalse(at(14).isCovered)
                field.filterNeighborsOf(at(14)).forEach {
                    assertTrue(it.isCovered)
                }

                field.filter { it.hasMine }.forEach {
                    fakeSingleClick(it.id)
                    assertTrue(it.mark.isFlag())
                }

                fakeDoubleClick(14)
                field.filterNeighborsOf(at(14)).forEach {
                    if (it.hasMine) {
                        assertTrue(it.isCovered)
                    } else {
                        assertFalse(it.isCovered)
                    }
                }
            }
        }
    }

    @Test
    fun testIfDoubleClickPlantMinesOnFirstClick() {
        withGameController(clickOnCreate = false) { controller ->
            controller.run {
                updateGameControl(GameControl.fromControlType(ControlStyle.DoubleClick))
                assertFalse(hasMines)
                assertEquals(0, field.filterNot { it.isCovered }.count())
                fakeSingleClick(40)
                assertTrue(hasMines)
                field.filterNeighborsOf(at(40)).forEach { assertFalse(it.isCovered) }
            }
        }
    }

    @Test
    fun testIfFastFlagPlantMinesOnFirstClick() {
        withGameController(clickOnCreate = false) { controller ->
            controller.run {
                updateGameControl(GameControl.fromControlType(ControlStyle.FastFlag))
                assertFalse(hasMines)
                assertEquals(0, field.filterNot { it.isCovered }.count())
                fakeSingleClick(40)
                assertTrue(hasMines)
                field.filterNeighborsOf(at(40)).forEach { assertFalse(it.isCovered) }
            }
        }
    }

    private fun GameController.at(index: Int): Area {
        return this.field.first { it.id == index }
    }

    private fun GameController.fakeSingleClick(index: Int) {
        runBlocking {
            launch {
                singleClick(index).collect {
                    it.second.collect()
                }
            }
        }
    }

    private fun GameController.fakeLongPress(index: Int) {
        runBlocking {
            launch {
                longPress(index).collect { it.second.collect() }
            }
        }
    }

    private fun GameController.fakeDoubleClick(index: Int) {
        runBlocking {
            launch {
                doubleClick(index).collect { it.second.collect() }
            }
        }
    }
}
