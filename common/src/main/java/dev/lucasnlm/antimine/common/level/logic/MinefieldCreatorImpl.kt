package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.logic.MinefieldExt.filterNeighborsOf
import dev.lucasnlm.antimine.common.level.logic.MinefieldExt.filterNotNeighborsOf
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.preferences.models.Minefield
import java.util.*
import kotlin.math.floor

class MinefieldCreatorImpl(
    private val minefield: Minefield,
    private val seed: Long,
) : MinefieldCreator {
    private fun createMutableEmpty(): List<Area> {
        val width = minefield.width
        val height = minefield.height
        val fieldLength = width * height

        val list =
            (0 until fieldLength).map { index ->
                val yPosition = floor((index / width).toDouble()).toInt()
                val xPosition = (index % width)
                Area(
                    index,
                    xPosition,
                    yPosition,
                    0,
                    hasMine = false,
                    neighborsIds = emptyList(),
                )
            }

        return list.map {
            it.copy(neighborsIds = list.filterNeighborsOf(it).map { area -> area.id })
        }
    }

    override fun createEmpty(): List<Area> {
        return createMutableEmpty()
    }

    override suspend fun create(safeIndex: Int): List<Area> {
        return createMutableEmpty().toMutableList().apply {
            filterNotNeighborsOf(safeIndex)
                .shuffled(Random(seed))
                .take(minefield.mines)
                .onEach {
                    it.neighborsIds.forEach { neighborId ->
                        val neighbor = this[neighborId]
                        this[neighborId] =
                            this[neighborId].copy(
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
