package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.logic.MinefieldExt.filterNeighborsOf
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

    private fun getAreaFor(
        index: Int,
        value: Char,
        width: Int,
    ): Area {
        val yPosition = floor((index / width).toDouble()).toInt()
        val xPosition = (index % width)
        return Area(
            id = index,
            posX = xPosition,
            posY = yPosition,
            minesAround = 0,
            hasMine = value == '1',
            neighborsIds = emptyList(),
        )
    }

    override suspend fun create(safeIndex: Int): List<Area> {
        return withContext(Dispatchers.IO) {
            val x = safeIndex % minefield.width
            val y = safeIndex / minefield.width
            val width = minefield.width

            val sliceWidth = getSliceWidth(minefield.width)
            val nativeResult =
                sgTathamMines.createMinefield(
                    seed = seed,
                    sliceWidth = sliceWidth,
                    width = minefield.width,
                    height = minefield.height,
                    mines = minefield.mines,
                    x = x,
                    y = y,
                )

            val resultList =
                if (sliceWidth != NO_SLICE) {
                    val slices = nativeResult.split(SLICE_DIVIDER).map { it.toList() }
                    val size = minefield.width * minefield.height
                    var index = 0
                    var line = 0

                    buildList {
                        while (index < size) {
                            slices.forEach { slice ->
                                slice
                                    .drop(line * sliceWidth)
                                    .take(sliceWidth)
                                    .map { char ->
                                        getAreaFor(
                                            index,
                                            char,
                                            width,
                                        ).also {
                                            index++
                                        }
                                    }
                                    .also(::addAll)
                            }
                            line++
                        }
                    }
                } else {
                    nativeResult.mapIndexed { index, char ->
                        getAreaFor(
                            index,
                            char,
                            width,
                        )
                    }
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

    private fun getSliceWidth(width: Int): Int {
        return if (width < 50) {
            NO_SLICE
        } else {
            listOf(
                20 to width % 20,
                25 to width % 25,
                22 to width % 22,
                24 to width % 24,
                width / 4 to width % 4,
                width / 3 to width % 3,
                width / 2 to width % 2,
            ).firstOrNull { it.second == 0 }?.first ?: NO_SLICE
        }
    }

    companion object {
        private const val SLICE_DIVIDER = ","
        private const val NO_SLICE = -1
    }
}
