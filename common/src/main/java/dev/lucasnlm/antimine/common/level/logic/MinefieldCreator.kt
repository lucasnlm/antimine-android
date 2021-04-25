package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlin.math.floor
import kotlin.random.Random

class MinefieldCreator(
    private val minefield: Minefield,
    private val randomGenerator: Random,
) {
    private fun createMutableEmpty(): List<Area> {
        val width = minefield.width
        val height = minefield.height
        val fieldLength = width * height

        val list = (0 until fieldLength).map { index ->
            val yPosition = floor((index / width).toDouble()).toInt()
            val xPosition = (index % width)
            Area(
                index,
                xPosition,
                yPosition,
                0,
                hasMine = false,
                neighbors = emptyList(),
            )
        }

        return list.map {
            it.copy(neighbors = list.filterNeighborsOf(it).map { area -> area.id })
        }
    }

    fun createEmpty(): List<Area> {
        return createMutableEmpty()
    }

    fun create(safeIndex: Int, safeZone: Boolean): List<Area> {
        return createMutableEmpty().toMutableList().apply {
            // Plant mines and setup number tips
            if (safeZone) {
                filterNotNeighborsOf(safeIndex)
            } else {
                filterNot { it.id == safeIndex }
            }
                .shuffled(randomGenerator)
                .take(minefield.mines)
                .onEach {
                    it.neighbors.forEach { neighborId ->
                        val neighbor = this[neighborId]
                        this[neighborId] = this[neighborId].copy(
                            minesAround = neighbor.minesAround + 1,
                        )
                    }
                }
                .onEach {
                    this[it.id] = this[it.id].copy(hasMine = true, minesAround = 0)
                }
        }
    }
}
