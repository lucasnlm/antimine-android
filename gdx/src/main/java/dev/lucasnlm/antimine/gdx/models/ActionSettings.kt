package dev.lucasnlm.antimine.gdx.models

data class ActionSettings(
    val freeControl: Boolean,
    val handleDoubleTaps: Boolean,
    val longTapTimeout: Long,
    val doubleTapTimeout: Long,
)
