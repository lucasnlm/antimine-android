package dev.lucasnlm.antimine.gdx.actors

import dev.lucasnlm.antimine.gdx.AtlasNames

data class AreaForm(
    val top: Boolean,
    val bottom: Boolean,
    val left: Boolean,
    val right: Boolean,
    val topLeft: Boolean = false,
    val topRight: Boolean = false,
    val bottomLeft: Boolean = false,
    val bottomRight: Boolean = false,
) {
    fun getAtlasNames(): Set<String> {
        return mapOf(
            AtlasNames.core to true,
            AtlasNames.top to top,
            AtlasNames.left to left,
            AtlasNames.bottom to bottom,
            AtlasNames.right to right,
            AtlasNames.cornerTopLeft to (!top && !left),
            AtlasNames.cornerTopRight to (!top && !right),
            AtlasNames.cornerBottomLeft to (!bottom && !left),
            AtlasNames.cornerBottomRight to (!bottom && !right),
            AtlasNames.borderCornerTopRight to (top && right && !topRight),
            AtlasNames.borderCornerTopLeft to (top && left && !topLeft),
            AtlasNames.borderCornerBottomRight to (bottom && right && !bottomRight),
            AtlasNames.borderCornerBottomLeft to (bottom && left && !bottomLeft),
            AtlasNames.fillTopLeft to (top && left && topLeft),
            AtlasNames.fillTopRight to (top && right && topRight),
            AtlasNames.fillBottomLeft to (bottom && left && bottomLeft),
            AtlasNames.fillBottomRight to (bottom && right && bottomRight),
        ).filter {
            it.value
        }.keys
    }
}

val areaNoForm = AreaForm(
    top = false,
    bottom = false,
    left = false,
    right = false,
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
