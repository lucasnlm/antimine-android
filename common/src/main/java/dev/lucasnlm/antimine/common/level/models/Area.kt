package dev.lucasnlm.antimine.common.level.models

data class Area(
    val id: Int,
    val posX: Int,
    val posY: Int,
    var minesAround: Int = 0,
    var hasMine: Boolean = false,
    var mistake: Boolean = false,
    var isCovered: Boolean = true,
    var mark: Mark = Mark.None,
    var highlighted: Boolean = false
)
