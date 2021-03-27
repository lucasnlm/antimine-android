package dev.lucasnlm.antimine.core

import dev.lucasnlm.antimine.core.models.Area

fun List<Area>.getPos(x: Int, y: Int): Area? {
    return firstOrNull { it.posX == x && it.posY == y }
}
