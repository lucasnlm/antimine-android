package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark

/**
 * This class is responsible for flagging squares that are isolated.
 * @param field The field to be flagged.
 */
class FlagAssistant(
    private val field: MutableList<Area>,
) {
    /**
     * Runs the flag assistant.
     */
    fun runFlagAssistant() {
        // Must not select Mark.PurposefulNone, only Mark.None. Otherwise, it will flag
        // a square that was previously unflagged by player.
        field
            .filter { it.hasMine && it.mark.isPureNone() }
            .forEach(::putFlagIfIsolated)
    }

    fun result(): List<Area> = field.toList()

    private fun putFlagIfIsolated(it: Area) {
        val neighbors = it.neighborsIds
        val neighborsCount = neighbors.count()
        val revealedNeighborsCount =
            neighbors.count { neighborId ->
                val neighbor = field[neighborId]
                !neighbor.isCovered || (neighbor.hasMine && neighbor.mark.isFlag())
            }

        if (revealedNeighborsCount == neighborsCount) {
            field[it.id] = it.copy(mark = Mark.Flag)
        }
    }
}
