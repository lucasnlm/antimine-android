package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.sgtatham.SgTathamMines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.floor

class MinefieldCreatorNativeImpl(
    private val minefield: Minefield,
    private val seed: Long,
) : MinefieldCreator {
    private val sgTathamMines: SgTathamMines = SgTathamMines()

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

    override suspend fun create(safeIndex: Int): List<Area> =
        withContext(Dispatchers.IO) {
            val x = safeIndex % minefield.width
            val y = safeIndex / minefield.width
            val width = minefield.width

            val nativeResult =
                sgTathamMines.createMinefield(
                    seed = seed,
                    width = minefield.width,
                    height = minefield.height,
                    mines = minefield.mines,
                    x = x,
                    y = y,
                )

            val resultList =
                nativeResult.mapIndexed { index, char ->
                    val yPosition = floor((index / width).toDouble()).toInt()
                    val xPosition = (index % width)
                    Area(
                        id = index,
                        posX = xPosition,
                        posY = yPosition,
                        minesAround = 0,
                        hasMine = char == '1',
                        neighborsIds = emptyList(),
                    )
                }

            resultList.map { area ->
                area.copy(neighborsIds = resultList.filterNeighborsOf(area).map { it.id })
            }.toMutableList().apply {
                // Ensure the first click and surround won't have a mine.
                filterNeighborsOf(safeIndex).forEach {
                    this[it.id] = this[it.id].copy(hasMine = false)
                }
                this[safeIndex] = this[safeIndex].copy(hasMine = false)

                filter {
                    it.hasMine
                }.onEach {
                    it.neighborsIds.forEach { neighborId ->
                        val neighbor = this[neighborId]
                        this[neighborId] = this[neighborId].copy(minesAround = neighbor.minesAround + 1)
                    }
                }.onEach {
                    this[it.id] = this[it.id].copy(hasMine = true, minesAround = 0)
                }
            }.toList()
        }
}
