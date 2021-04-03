package dev.lucasnlm.antimine.gdx.models

import dev.lucasnlm.antimine.core.models.Area

data class TouchAreaAction(
    val area: Area,
    val pressedAt: Long,
    val releasedAt: Long?,
    val consumed: Boolean,
    val x: Float,
    val y: Float,
)
