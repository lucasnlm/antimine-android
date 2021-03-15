package dev.lucasnlm.antimine.core

import dev.lucasnlm.antimine.core.models.Area
import kotlin.math.absoluteValue

fun Area.isNeighborOf(area: Area): Boolean {
    return ((posX - area.posX).absoluteValue <= 1 && (posY - area.posY).absoluteValue <= 1) && id != area.id
}

fun List<Area>.getPos(x: Int, y: Int): Area? {
    return firstOrNull { it.posX == x && it.posY == y }
}
