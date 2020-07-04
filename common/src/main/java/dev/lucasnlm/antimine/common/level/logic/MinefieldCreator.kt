package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.Minefield
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.random.Random

class MinefieldCreator(
    private val minefield: Minefield,
    private val randomGenerator: Random
) {
    private class MapItem(
        val id: Int,
        val posX: Int,
        val posY: Int,
        var minesAround: Int = 0,
        var hasMine: Boolean = false
    )

    private fun createEmptyField(): List<MapItem> {
        val width = minefield.width
        val height = minefield.height
        val fieldSize = width * height

        return (0 until fieldSize).map { index ->
            val yPosition = floor((index / width).toDouble()).toInt()
            val xPosition = (index % width)
            MapItem(
                index,
                xPosition,
                yPosition
            )
        }.toList()
    }

    fun createMap(safeIndex: Int): List<Area> {
        val minefieldMap = createEmptyField()
        val safeArea = minefieldMap.getArea(safeIndex)

        // Plant mines and setup number tips
        minefieldMap
            .filterNot {
                ((it.posX - safeArea.posX).absoluteValue <= 1 && (it.posY - safeArea.posY).absoluteValue <= 1)
            }
            .toSet()
            .shuffled(randomGenerator)
            .take(minefield.mines)
            .onEach {
                it.hasMine = true
                it.findNeighbors(minefieldMap).forEach { neighbor ->
                    neighbor.minesAround = neighbor.minesAround + 1
                }
            }
            .onEach {
                it.minesAround = 0
            }

        return minefieldMap.map {
            Area(it.id, it.posX, it.posY, it.minesAround, false, it.hasMine)
        }.toList()
    }

    private fun List<MapItem>.getArea(id: Int) = this.first { it.id == id }

    private fun MapItem.findNeighbors(onMap: List<MapItem>) = sequenceOf(
        1 to 0, 1 to 1, 0 to 1, -1 to 1, -1 to 0, -1 to -1, 0 to -1, 1 to -1
    ).map { (x, y) -> getNeighbor(x, y, onMap) }.filterNotNull()

    private fun MapItem.getNeighbor(x: Int, y: Int, onMap: List<MapItem>) = onMap.firstOrNull {
        (it.posX == this.posX + x) && (it.posY == this.posY + y)
    }
}
