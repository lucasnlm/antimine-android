package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.models.Area
import kotlin.math.absoluteValue

fun List<Area>.firstArea(id: Int) = this.first { it.id == id }

fun List<Area>.filterNeighborsOf(area: Area) = this.filter {
    ((it.posX - area.posX).absoluteValue <= 1 && (it.posY - area.posY).absoluteValue <= 1)
}.filterNot { area.id == it.id }

fun List<Area>.filterNotNeighborsOf(area: Area) = this.filter {
    ((it.posX - area.posX).absoluteValue > 1 || (it.posY - area.posY).absoluteValue > 1)
}

fun List<Area>.filterNotNeighborsOf(areaIndex: Int) = this.filterNotNeighborsOf(this.firstArea(areaIndex))
