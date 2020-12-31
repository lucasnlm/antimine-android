package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark

class FlagAssistant(
    private val field: MutableList<dev.lucasnlm.antimine.core.models.Area>,
) {
    fun runFlagAssistant() {
        // Must not select Mark.PurposefulNone, only Mark.None. Otherwise, it will flag
        // a square that was previously unflagged by player.
        field
            .filter { it.hasMine && it.mark.isPureNone() }
            .forEach(::putFlagIfIsolated)
    }

    fun result(): List<dev.lucasnlm.antimine.core.models.Area> = field.toList()

    private fun putFlagIfIsolated(it: dev.lucasnlm.antimine.core.models.Area) {
        val neighbors = field.filterNeighborsOf(it)
        val neighborsCount = neighbors.count()
        val revealedNeighborsCount = neighbors.count { neighbor ->
            !neighbor.isCovered || (neighbor.hasMine && neighbor.mark.isFlag())
        }

        if (revealedNeighborsCount == neighborsCount) {
            field[it.id] = it.copy(mark = dev.lucasnlm.antimine.core.models.Mark.Flag)
        }
    }
}
