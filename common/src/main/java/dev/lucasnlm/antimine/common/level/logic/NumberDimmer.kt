package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark

/**
 * This class is responsible for dimming numbers.
 */
class NumberDimmer(
    private val field: MutableList<Area>,
) {
    fun runDimmer() {
        field
            .filter { it.minesAround > 0 }
            .forEach(::dimIfNumberMatchesFlags)
    }

    fun runDimmerAll() {
        field
            .filter { it.minesAround > 0 }
            .forEach(::dim)
    }

    fun result(): List<Area> = field.toList()

    private fun dimIfNumberMatchesFlags(it: Area) {
        val neighbors = it.neighborsIds
        val shouldDim =
            neighbors.count {
                val neighbor = field[it]
                neighbor.isCovered && neighbor.mark == Mark.Flag
            } == it.minesAround
        field[it.id] = it.copy(dimNumber = shouldDim)
    }

    private fun dim(it: Area) {
        field[it.id] = it.copy(dimNumber = true)
    }
}
