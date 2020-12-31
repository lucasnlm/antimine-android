package dev.lucasnlm.antimine.core.models

data class Area(
    val id: Int,
    val posX: Int,
    val posY: Int,
    val minesAround: Int = 0,
    val hasMine: Boolean = false,
    val mistake: Boolean = false,
    val isCovered: Boolean = true,
    val mark: Mark = Mark.None,
    val highlighted: Boolean = false,
    val revealed: Boolean = false,
)
