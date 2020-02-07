package dev.lucasnlm.antimine.common.level.data

data class Area(
    val id: Int,
    val posX: Int,
    val posY: Int,
    var minesAround: Int = 0,
    var safeZone: Boolean = false,
    var hasMine: Boolean = false,
    var mistake: Boolean = false,
    var isCovered: Boolean = true,
    var mark: Mark = Mark.None,
    var highlighted: Boolean = false
)
