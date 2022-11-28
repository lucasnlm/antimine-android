package dev.lucasnlm.antimine.preferences.models

data class Minefield(
    val width: Int,
    val height: Int,
    val mines: Int,
    val seed: Long? = null,
)
