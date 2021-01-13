package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark

class MinefieldHandler(
    private val field: MutableList<Area>,
    private val useQuestionMark: Boolean,
) {
    fun showAllMines() {
        field.filter { it.hasMine && it.mark != Mark.Flag }
            .forEach { field[it.id] = it.copy(isCovered = false) }
    }

    fun flagAllMines() {
        field.filter { it.hasMine && it.isCovered }
            .forEach { field[it.id] = it.copy(mark = Mark.Flag) }
    }

    fun revealAllEmptyAreas() {
        field.filterNot { it.hasMine }
            .forEach { field[it.id] = it.copy(isCovered = false) }
    }

    fun revealRandomMine(): Boolean {
        return field.filter { it.hasMine && it.mark.isNone() && !it.revealed }.shuffled().firstOrNull()?.run {
            field[this.id] = this.copy(revealed = true)
            true
        } ?: false
    }

    fun turnOffAllHighlighted() {
        field.filter { it.highlighted }
            .forEach { field[it.id] = it.copy(highlighted = false) }
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
                    }
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
                    mistake = (!passive && hasMine) || (!hasMine && mark.isFlag())
                )

                if (!hasMine && minesAround == 0 && openNeighbors) {
                    field.filterNeighborsOf(this)
                        .filter { it.isCovered }
                        .onEach {
                            openAt(it.id, openNeighbors = true, passive = true)
                        }.count()
                }
            }
        }
    }

    fun highlightAt(index: Int) {
        field.getOrNull(index)?.let { target ->
            if (!target.isCovered) {
                field[index] = target.copy(highlighted = target.minesAround != 0 && !target.highlighted)

                field.filterNeighborsOf(target)
                    .filter { it.mark.isNone() && it.isCovered }
                    .onEach { neighbor ->
                        field[neighbor.id] = neighbor.copy(highlighted = true)
                    }
            }
        }
    }

    fun openOrFlagNeighborsOf(index: Int) {
        field.getOrNull(index)?.run {
            if (!isCovered) {
                val neighbors = field.filterNeighborsOf(this)
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

    fun result(): List<Area> = field.toList()
}
