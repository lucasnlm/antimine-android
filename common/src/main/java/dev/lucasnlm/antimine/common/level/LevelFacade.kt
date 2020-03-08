package dev.lucasnlm.antimine.common.level

import dev.lucasnlm.antimine.common.level.data.LevelSetup
import dev.lucasnlm.antimine.common.level.data.Area
import dev.lucasnlm.antimine.common.level.data.GameStats
import dev.lucasnlm.antimine.common.level.data.Mark
import dev.lucasnlm.antimine.common.level.database.data.Save
import dev.lucasnlm.antimine.common.level.database.data.SaveStatus
import java.util.Random
import kotlin.math.floor

class LevelFacade {
    private val levelSetup: LevelSetup
    private val randomGenerator: Random
    private var saveId = 0
    private val startTime = System.currentTimeMillis()

    var hasMines = false
        private set

    var seed = 0L
        private set

    lateinit var field: Sequence<Area>
        private set

    private var mines: Sequence<Area> = sequenceOf()

    constructor(gameId: Int, levelSetup: LevelSetup, seed: Long = randomSeed()) {
        this.saveId = gameId
        this.levelSetup = levelSetup
        this.randomGenerator = Random().apply { setSeed(seed) }
        this.seed = seed
        createEmptyField()
    }

    constructor(save: Save) {
        this.saveId = save.uid
        this.levelSetup = save.levelSetup
        this.randomGenerator = Random().apply { setSeed(save.seed) }
        this.field = save.field.asSequence()
        this.mines = this.field.filter { it.hasMine }.asSequence()
        this.hasMines = this.mines.count() != 0
    }

    private fun createEmptyField() {
        val width = levelSetup.width
        val height = levelSetup.height
        val fieldSize = width * height
        this.field = (0 until fieldSize).map { index ->
            val yPosition = floor((index / width).toDouble()).toInt()
            val xPosition = (index % width)
            Area(index, xPosition, yPosition)
        }.asSequence()
    }

    private fun getArea(id: Int) = field.first { it.id == id }

    fun switchMarkAt(index: Int): Boolean {
        val changed: Boolean
        getArea(index).apply {
            changed = isCovered
            if (isCovered) {
                mark = when (mark) {
                    Mark.PurposefulNone, Mark.None -> Mark.Flag
                    Mark.Flag -> Mark.Question
                    Mark.Question -> Mark.None
                }
            }
        }
        return changed
    }

    fun removeMark(index: Int) {
        getArea(index).apply {
            mark = Mark.PurposefulNone
        }
    }

    fun hasCoverOn(index: Int): Boolean = getArea(index).isCovered

    fun hasMarkOn(index: Int): Boolean = getArea(index).mark.run {
        this != Mark.None && this != Mark.PurposefulNone
    }

    fun plantMinesExcept(index: Int, includeSafeArea: Boolean = false) {
        plantRandomMines(index, includeSafeArea)
        putMinesTips()
    }

    private fun plantRandomMines(ignoreIndex: Int, includeSafeArea: Boolean) {
        getArea(ignoreIndex).run {
            safeZone = true

            if (includeSafeArea) {
                findNeighbors().forEach {
                    it.safeZone = true
                }
            }
        }

        field.filterNot { it.safeZone }
            .toSet()
            .shuffled(randomGenerator)
            .take(levelSetup.mines)
            .forEach { it.hasMine = true }
        mines = field.filter { it.hasMine }.asSequence()
        hasMines = mines.count() != 0
    }

    private fun putMinesTips() {
        field.forEach {
            it.minesAround = if (it.hasMine) 0 else it.findNeighbors().filter { neighbor ->
                neighbor.hasMine
            }.count()
        }
    }

    /**
     * Run "Flood Fill algorithm" to open all empty neighbors of a target area.
     */
    fun openField(target: Area): Boolean {
        val result: Boolean = target.isCovered

        if (target.isCovered) {
            target.isCovered = false
            target.mark = Mark.None

            if (target.minesAround == 0 && !target.hasMine) {
                target.findNeighbors().forEach { openField(it) }
            }

            if (target.hasMine) {
                target.mistake = true
            }
        }

        return result
    }

    fun turnOffAllHighlighted() {
        field.forEach {
            it.highlighted = false
        }
    }

    private fun toggleHighlight(target: Area) {
        target.highlighted = !target.highlighted
        target.findNeighbors()
            .filter { it.mark == Mark.None && it.isCovered }
            .forEach { it.highlighted = !it.highlighted }
    }

    fun clickArea(index: Int): Boolean = getArea(index).let {
        when {
            it.isCovered -> {
                openField(getArea(index))
            }
            it.minesAround != 0 -> {
                toggleHighlight(it)
                true
            }
            else -> {
                false
            }
        }
    }

    fun openNeighbors(index: Int): List<Int> =
        getArea(index)
            .findNeighbors()
            .filter {
                it.mark == Mark.None
            }
            .map {
                openField(it)
                it.id
            }

    fun runFlagAssistant() {
        mines.filter { it.mark == Mark.None }.forEach { field ->
            val neighbors = field.findNeighbors()
            val neighborsCount = neighbors.count()
            val revealedNeighborsCount = neighbors.filter { neighbor ->
                !neighbor.isCovered || (neighbor.hasMine && neighbor.mark == Mark.Flag)
            }.count()

            field.mark = if (revealedNeighborsCount == neighborsCount) Mark.Flag else Mark.None
        }
    }

    fun getStats() = GameStats(
            mines.filter { !it.mistake && it.mark == Mark.Flag }.count(),
            mines.count(),
            field.count()
        )

    fun showAllMines() {
        mines.filter { it.mark != Mark.Flag }.forEach { it.isCovered = false }
    }

    fun flagAllMines() {
        mines.forEach { it.mark = Mark.Flag }
    }

    fun showWrongFlags() {
        field.filter { it.mark != Mark.None && !it.hasMine }.forEach { it.mistake = true }
    }

    fun revealAllEmptyAreas() {
        field.filter { !it.hasMine }.forEach { it.isCovered = false }
    }

    fun hasAnyMineExploded(): Boolean = mines.firstOrNull { it.mistake } != null

    fun hasFlaggedAllMines(): Boolean = rightFlags() == levelSetup.mines

    fun hasIsolatedAllMines() =
        mines.map {
            val neighbors = it.findNeighbors()
            val neighborsCount = neighbors.count()
            val isolatedNeighborsCount = neighbors.filter { neighbor ->
                !neighbor.isCovered || neighbor.hasMine
            }.count()
            neighborsCount == isolatedNeighborsCount
        }.filterNot { it }.count() == 0

    private fun rightFlags() = mines.count { it.mark == Mark.Flag }

    fun checkVictory(): Boolean =
        hasMines && hasIsolatedAllMines() && !hasAnyMineExploded()

    fun remainingMines(): Int {
        val flagsCount = field.count { it.mark == Mark.Flag }
        val minesCount = mines.count()
        return (minesCount - flagsCount).coerceAtLeast(0)
    }

    private fun Area.findNeighbors() = arrayOf(
        getNeighbor(1, 0),
        getNeighbor(1, 1),
        getNeighbor(0, 1),
        getNeighbor(-1, 1),
        getNeighbor(-1, 0),
        getNeighbor(-1, -1),
        getNeighbor(0, -1),
        getNeighbor(1, -1)
    ).filterNotNull()

    private fun Area.getNeighbor(x: Int, y: Int) = field.firstOrNull {
        (it.posX == this.posX + x) && (it.posY == this.posY + y)
    }

    fun getSaveState(): Save {
        val saveStatus: SaveStatus = when {
            checkVictory() -> SaveStatus.VICTORY
            hasAnyMineExploded() -> SaveStatus.DEFEAT
            else -> SaveStatus.ON_GOING
        }
        return Save(saveId, seed, startTime, 0L, levelSetup, saveStatus, field.toList())
    }

    companion object {
        fun randomSeed(): Long = Random().nextLong()
    }
}
