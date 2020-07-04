package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.Mark
import kotlin.math.absoluteValue

class MinefieldHandler(
    private val minefield: MutableList<Area>
) {

    fun openArea(index: Int) {
        val area = minefield[index]
        minefield[index] = area.copy(
            isCovered = false,
            mark = Mark.None,
            mistake = area.hasMine
        )

        if (area.minesAround == 0) {
            minefield.filter {
                it.isCovered && ((it.posX - area.posX).absoluteValue == 1 || (it.posY - area.posY).absoluteValue == 1)
            }.forEach {
                openArea(it.id)
            }
        }
    }

    fun result(): List<Area> = minefield.toList()
}
