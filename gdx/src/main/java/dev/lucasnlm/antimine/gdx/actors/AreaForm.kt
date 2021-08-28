package dev.lucasnlm.antimine.gdx.actors

data class AreaForm(
    val top: Boolean,
    val bottom: Boolean,
    val left: Boolean,
    val right: Boolean,
    val topLeft: Boolean = false,
    val topRight: Boolean = false,
    val bottomLeft: Boolean = false,
    val bottomRight: Boolean = false,
)

val areaNoForm = AreaForm(
    top = false,
    bottom = false,
    left = false,
    right = false
)

val areaFullForm = AreaForm(
    top = true,
    bottom = true,
    left = true,
    right = true,
    topLeft = true,
    topRight = true,
    bottomLeft = true,
    bottomRight = true,
)

object FormNames {
    const val core = "core"
    const val bottom = "b"
    const val top = "t"
    const val right = "r"
    const val left = "l"
    const val cornerTopLeft = "c-t-l"
    const val cornerTopRight = "c-t-r"
    const val cornerBottomRight = "c-b-r"
    const val cornerBottomLeft = "c-b-l"
    const val borderCornerTopRight = "bc-t-r"
    const val borderCornerTopLeft = "bc-t-l"
    const val borderCornerBottomRight = "bc-b-r"
    const val borderCornerBottomLeft = "bc-b-l"
    const val fillTopLeft = "t-l-tl"
    const val fillTopRight = "t-r-tr"
    const val fillBottomRight = "b-r-br"
    const val fillBottomLeft = "b-l-bl"
    const val full = "full"
}
