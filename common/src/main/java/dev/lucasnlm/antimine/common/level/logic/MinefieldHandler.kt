package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark
import kotlin.math.absoluteValue

/**
 * This class is responsible for handling the minefield.
 * @param field The field to be handled.
 * @param useQuestionMark Whether to use the question mark mark.
 * @param individualActions Whether to use individual actions.
 */
class MinefieldHandler(
    private val field: MutableList<Area>,
    private val useQuestionMark: Boolean,
    private val individualActions: Boolean,
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

    private fun Area.hasUncoveredNeighbor(): Boolean {
        return neighborsIds.toArea().any { !it.isCovered }
    }

    private fun Area.potentialMineReveal(): Boolean {
        return hasMine && mark.isNone() && !revealed && isCovered
    }

    fun revealRandomMineNearUncoveredArea(
        visibleMines: Set<Int>,
        lastX: Int? = null,
        lastY: Int? = null,
    ): Int? {
        // / Prioritized mines are mines that are visible and have a potential to be revealed.
        // / If there are no prioritized mines, then we get all mines that have a potential to be revealed.
        val prioritizedMines =
            visibleMines
                .toArea()
                .filter { it.potentialMineReveal() && it.hasUncoveredNeighbor() }

        val unrevealedMinesWithUncoveredNeighbor =
            prioritizedMines.ifEmpty {
                field.filter { it.potentialMineReveal() && it.hasUncoveredNeighbor() }
            }

        val unrevealedMines =
            unrevealedMinesWithUncoveredNeighbor.ifEmpty {
                field.filter { it.potentialMineReveal() }
            }

        val nearestTarget =
            if (lastX != null && lastY != null) {
                unrevealedMines.filter {
                    (lastX - it.posX).absoluteValue < NEAR_MINE_THRESHOLD &&
                        (lastY - it.posY).absoluteValue < NEAR_MINE_THRESHOLD
                }.shuffled().firstOrNull()
            } else {
                null
            }

        return when {
            nearestTarget != null -> {
                field[nearestTarget.id] = nearestTarget.copy(revealed = true)
                nearestTarget.id
            }
            else -> {
                unrevealedMines.shuffled().firstOrNull()?.run {
                    field[this.id] = this.copy(revealed = true)
                    this.id
                }
            }
        }
    }

    fun removeMarkAt(index: Int) {
        field.getOrNull(index)?.let {
            field[it.id] = it.copy(mark = Mark.PurposefulNone)
        }
    }

    fun toggleMarkAt(
        index: Int,
        mark: Mark,
    ) {
        field.getOrNull(index)?.let {
            field[it.id] =
                if (it.mark.isNone()) {
                    it.copy(mark = mark)
                } else {
                    it.copy(mark = Mark.None)
                }
        }
    }

    fun switchMarkAt(index: Int) {
        field.getOrNull(index)?.let {
            if (it.isCovered) {
                field[index] =
                    it.copy(
                        mark =
                            when (it.mark) {
                                Mark.PurposefulNone, Mark.None -> Mark.Flag
                                Mark.Flag -> if (useQuestionMark && !individualActions) Mark.Question else Mark.None
                                Mark.Question -> Mark.None
                            },
                    )
            }
        }
    }

    fun openAt(
        index: Int,
        passive: Boolean,
        openNeighbors: Boolean = true,
    ) {
        field.getOrNull(index)?.run {
            if (isCovered) {
                field[index] =
                    copy(
                        isCovered = false,
                        mark = Mark.None,
                        mistake = (!passive && hasMine) || (!hasMine && mark.isFlag()),
                    )

                if (!hasMine && minesAround == 0 && openNeighbors) {
                    neighborsIds
                        .toArea()
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
                val neighbors = neighborsIds.toArea()
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
                neighborsIds
                    .toArea()
                    .filter { it.isCovered && it.mark.isNone() }
                    .forEach { openAt(it.id, passive = false, openNeighbors = true) }
            }
        }
    }

    fun result(): List<Area> = field.toList()

    private fun Collection<Int>.toArea(): Collection<Area> = map { field[it] }

    companion object {
        const val NEAR_MINE_THRESHOLD = 5
    }
}
