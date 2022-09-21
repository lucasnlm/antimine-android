package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark
import kotlin.math.absoluteValue

class MinefieldHandler(
    private val field: MutableList<Area>,
    private val useQuestionMark: Boolean,
) {
    fun showAllMines() {
        field.filter { it.hasMine && it.mark != Mark.Flag }
            .forEach { field[it.id] = it.copy(isCovered = false) }
    }

    fun showAllWrongFlags() {
        field.filter { !it.hasMine && it.mark.isNotNone() }
            .forEach { field[it.id] = it.copy(mistake = true) }
    }

    fun flagAllMines() {
        field.filter { it.hasMine && it.isCovered }
            .forEach { field[it.id] = it.copy(mark = Mark.Flag) }
    }

    fun revealAllEmptyAreas() {
        field.filterNot { it.hasMine }
            .forEach { field[it.id] = it.copy(isCovered = false) }
    }

    fun dismissMistake() {
        field.filter { it.hasMine && it.mistake }
            .forEach { field[it.id] = it.copy(mistake = false) }
    }

    fun revealRandomMineNearUncoveredArea(lastX: Int? = null, lastY: Int? = null): Boolean {
        val unrevealedMines = field.filter { it.hasMine && it.mark.isNone() && !it.revealed }
        val nearestTarget = if (lastX != null && lastY != null) {
            unrevealedMines.filter {
                (lastX - it.posX).absoluteValue < 3 && (lastY - it.posY).absoluteValue < 3
            }.shuffled().firstOrNull()
        } else {
            null
        }

        val result = when {
            nearestTarget != null -> {
                field[nearestTarget.id] = nearestTarget.copy(revealed = true)
                true
            }
            else -> {
                unrevealedMines.shuffled().firstOrNull()?.run {
                    field[this.id] = this.copy(revealed = true)
                    true
                }
            }
        }

        return result ?: false
    }

    fun removeMarkAt(index: Int) {
        field.getOrNull(index)?.let {
            field[it.id] = it.copy(mark = Mark.PurposefulNone)
        }
    }

    fun switchMarkAt(index: Int) {
        field.getOrNull(index)?.let {
            if (it.isCovered) {
                field[index] = it.copy(
                    mark = when (it.mark) {
                        Mark.PurposefulNone, Mark.None -> Mark.Flag
                        Mark.Flag -> if (useQuestionMark) Mark.Question else Mark.None
                        Mark.Question -> Mark.None
                    },
                )
            }
        }
    }

    fun openAt(index: Int, passive: Boolean, openNeighbors: Boolean = true) {
        field.getOrNull(index)?.run {
            if (isCovered) {
                field[index] = copy(
                    isCovered = false,
                    mark = Mark.None,
                    mistake = (!passive && hasMine) || (!hasMine && mark.isFlag()),
                )

                if (!hasMine && minesAround == 0 && openNeighbors) {
                    neighborsIds
                        .map { field[it] }
                        .filter { it.isCovered }
                        .onEach {
                            openAt(it.id, openNeighbors = true, passive = true)
                        }.count()
                }
            }
        }
    }

    fun openOrFlagNeighborsOf(index: Int) {
        field.getOrNull(index)?.run {
            if (!isCovered) {
                val neighbors = neighborsIds.map { field[it] }
                val flaggedCount = neighbors.count { it.mark.isFlag() || (!it.isCovered && it.hasMine) }
                if (flaggedCount >= minesAround) {
                    neighbors
                        .filter { it.isCovered && it.mark.isNone() }
                        .forEach { openAt(it.id, passive = false, openNeighbors = true) }
                } else {
                    val coveredNeighbors = neighbors.filter { it.isCovered }
                    val minesAround = neighbors.count { it.hasMine && it.isCovered }
                    if (coveredNeighbors.count() == minesAround) {
                        coveredNeighbors.filter {
                            it.mark.isNone()
                        }.forEach {
                            switchMarkAt(it.id)
                        }
                    }
                }
            }
        }
    }

    fun openNeighborsOf(index: Int) {
        field.getOrNull(index)?.run {
            if (!isCovered) {
                val neighbors = neighborsIds.map { field[it] }
                neighbors
                    .filter { it.isCovered && it.mark.isNone() }
                    .forEach { openAt(it.id, passive = false, openNeighbors = true) }
            }
        }
    }

    fun result(): List<Area> = field.toList()
}
