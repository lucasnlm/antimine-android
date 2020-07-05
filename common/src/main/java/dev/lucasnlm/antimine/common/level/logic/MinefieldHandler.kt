package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.Mark
import dev.lucasnlm.antimine.common.level.models.StateUpdate

class MinefieldHandler(
    private val field: MutableList<Area>,
    private val useQuestionMark: Boolean
) {
    private var changedIndex: Int? = null
    private var changes = 0

    fun turnOffAllHighlighted() {
        changes = field.filter { it.highlighted }.onEach { it.highlighted = false }.count()
    }

    fun removeMarkAt(index: Int) {
        field.getOrNull(index)?.let {
            changes++
            changedIndex = index
            it.mark = Mark.PurposefulNone
        }
    }

    fun switchMarkAt(index: Int) {
        field.getOrNull(index)?.run {
            if (isCovered) {
                changes++
                changedIndex = index
                mark = when (mark) {
                    Mark.PurposefulNone, Mark.None -> Mark.Flag
                    Mark.Flag -> if (useQuestionMark) Mark.Question else Mark.None
                    Mark.Question -> Mark.None
                }
            }
        }
    }

    fun openAt(index: Int) {
        field.getOrNull(index)?.run {
            if (isCovered) {
                changedIndex = index
                changes += 1
                isCovered = false
                mark = Mark.None

                if (hasMine) {
                    mistake = true
                } else if (minesAround == 0) {
                    changes +=
                        field.filterNeighborsOf(this)
                            .filter { it.isCovered }
                            .onEach {
                                openAt(it.id)
                            }.count()
                }
            }
        }
    }

    fun highlightAt(index: Int) {
        field.getOrNull(index)?.run {
            when {
                minesAround != 0 -> {
                    changes++
                    changedIndex = index
                    highlighted = !highlighted
                    changes += field.filterNeighborsOf(this)
                        .filter { it.mark.isNone() && it.isCovered }
                        .onEach { it.highlighted = !it.highlighted }
                        .count()
                }
                else -> 0
            }
        }
    }

    fun openNeighborsOf(index: Int) {
        field.getOrNull(index)?.run {
            val neighbors = field.filterNeighborsOf(this)
            val flaggedCount = neighbors.count { it.mark.isFlag() }
            if (flaggedCount >= minesAround) {
                changes += neighbors
                    .filter { it.mark.isNone() && it.isCovered }
                    .onEach { openAt(it.id) }
                    .count()
            }
        }
    }

    fun result(): List<Area> = field.toList()

    fun getStateUpdate(): StateUpdate {
        return when (changes) {
            0 -> {
                StateUpdate.None
            }
            1 -> {
                changedIndex?.let(StateUpdate::Single) ?: StateUpdate.Multiple
            }
            else -> {
                StateUpdate.Multiple
            }
        }
    }
}
