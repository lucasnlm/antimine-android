package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import kotlin.math.absoluteValue

object MinefieldExt {
    /**
     * Returns the area with the given id.
     */
    private fun List<Area>.withId(id: Int) = first { it.id == id }

    /**
     * Returns only the [Area]s that are neighbors of the given [Area].
     */
    fun List<Area>.filterNeighborsOf(area: Area) =
        filter {
            ((it.posX - area.posX).absoluteValue <= 1 && (it.posY - area.posY).absoluteValue <= 1)
        }.filterNot { area.id == it.id }

    /**
     * Returns only the [Area]s that are not neighbors of the given [Area].
     */
    fun List<Area>.filterNotNeighborsOf(area: Area) =
        filter {
            ((it.posX - area.posX).absoluteValue > 1 || (it.posY - area.posY).absoluteValue > 1)
        }

    /**
     * Returns only the [Area]s that are neighbors of the given [Area].
     */
    fun List<Area>.filterNeighborsOf(areaId: Int) = filterNeighborsOf(withId(areaId))

    /**
     * Returns only the [Area]s that are not neighbors of the given [Area].
     */
    fun List<Area>.filterNotNeighborsOf(areaId: Int) = filterNotNeighborsOf(withId(areaId))
}
