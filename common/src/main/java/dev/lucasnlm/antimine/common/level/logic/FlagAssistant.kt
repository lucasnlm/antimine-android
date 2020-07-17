package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.Mark
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class FlagAssistant(
    private val field: MutableList<Area>
) {
    @ExperimentalCoroutinesApi
    fun runFlagAssistant() = flow<Int> {
        // Must not select Mark.PurposefulNone, only Mark.None. Otherwise, it will flag
        // a square that was previously unflagged by player.
        val flaggedIds = field
            .filter { it.hasMine && it.mark.isPureNone() }
            .mapNotNull(::putFlagIfIsolated)
            .asFlow()

        emitAll(flaggedIds)
    }

    private fun putFlagIfIsolated(it: Area): Int? {
        val neighbors = field.filterNeighborsOf(it)
        val neighborsCount = neighbors.count()
        val revealedNeighborsCount = neighbors.count { neighbor ->
            !neighbor.isCovered || (neighbor.hasMine && neighbor.mark.isFlag())
        }

        return if (revealedNeighborsCount == neighborsCount) {
            it.mark = Mark.Flag
            it.id
        } else {
            null
        }
    }
}
