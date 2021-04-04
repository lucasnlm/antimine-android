package dev.lucasnlm.antimine.gdx.models

data class ActionSettings(
    val handleDoubleTaps: Boolean,
    val longTapTimeout: Long,
    val doubleTapTimeout: Long,
    val touchSensibility: Int,
)
