package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.Minefield
import kotlin.math.floor
import kotlin.random.Random

class MinefieldCreator(
    private val minefield: Minefield,
    private val roundedMap: Boolean,
    private val randomGenerator: Random
) {
    private fun createMutableEmpty(): MutableList<Area> {
        val width = minefield.width
        val height = minefield.height
        val fieldSize = width * height

        return (0 until fieldSize).map { index ->
            val yPosition = floor((index / width).toDouble()).toInt()
            val xPosition = (index % width)
            val enabled = !roundedMap || when (yPosition) {
                0 -> (xPosition > 1 && xPosition < width - 2)
                1 -> (xPosition > 0 && xPosition < width - 1)
                height - 1 -> (xPosition > 1 && xPosition < width - 2)
                height - 2 -> (xPosition > 0 && xPosition < width - 1)
                else -> true
            }
            Area(
                index,
                xPosition,
                yPosition,
                0,
                hasMine = false,
                enabled = enabled
            )
        }.toMutableList()
    }

    fun createEmpty(): List<Area> {
        return createMutableEmpty().toList()
    }

    fun create(safeIndex: Int, safeZone: Boolean): List<Area> {
        return createMutableEmpty().apply {
            // Plant mines and setup number tips
            if (safeZone) { filterNotNeighborsOf(safeIndex) } else { filterNot { it.id == safeIndex } }
                .shuffled(randomGenerator)
                .take(minefield.mines)
                .onEach {
                    filterNeighborsOf(it).forEach { neighbor ->
                        this[neighbor.id] = this[neighbor.id].copy(minesAround = neighbor.minesAround + 1)
                    }
                }
                .onEach {
                    this[it.id] = this[it.id].copy(hasMine = true, minesAround = 0)
                }
        }
    }
}
