package dev.lucasnlm.antimine.gdx.actors

import dev.lucasnlm.antimine.gdx.AtlasNames

object AreaFormFlag {
    const val TOP = 0b00000001
    const val BOTTOM = 0b00000010
    const val LEFT = 0b00000100
    const val RIGHT = 0b00001000
    const val TOP_LEFT = 0b00010000
    const val TOP_RIGHT = 0b00100000
    const val BOTTOM_LEFT = 0b01000000
    const val BOTTOM_RIGHT = 0b10000000
}

private fun Int.checkForm(flag: Int): Boolean {
    return (this and flag) != 0
}

fun Int.top(): Boolean = checkForm(AreaFormFlag.TOP)

fun Int.bottom(): Boolean = checkForm(AreaFormFlag.BOTTOM)

fun Int.left(): Boolean = checkForm(AreaFormFlag.LEFT)

fun Int.right(): Boolean = checkForm(AreaFormFlag.RIGHT)

fun Int.topLeft(): Boolean = checkForm(AreaFormFlag.TOP_LEFT)

fun Int.topRight(): Boolean = checkForm(AreaFormFlag.TOP_RIGHT)

fun Int.bottomLeft(): Boolean = checkForm(AreaFormFlag.BOTTOM_LEFT)

fun Int.bottomRight(): Boolean = checkForm(AreaFormFlag.BOTTOM_RIGHT)

fun areaFormOf(
    top: Boolean,
    bottom: Boolean,
    left: Boolean,
    right: Boolean,
    topLeft: Boolean = false,
    topRight: Boolean = false,
    bottomLeft: Boolean = false,
    bottomRight: Boolean = false,
): Int {
    var result = 0x00

    if (top) {
        result = result or AreaFormFlag.TOP
    }

    if (bottom) {
        result = result or AreaFormFlag.BOTTOM
    }

    if (left) {
        result = result or AreaFormFlag.LEFT
    }

    if (right) {
        result = result or AreaFormFlag.RIGHT
    }

    if (topLeft) {
        result = result or AreaFormFlag.TOP_LEFT
    }

    if (topRight) {
        result = result or AreaFormFlag.TOP_RIGHT
    }

    if (bottomLeft) {
        result = result or AreaFormFlag.BOTTOM_LEFT
    }

    if (bottomRight) {
        result = result or AreaFormFlag.BOTTOM_RIGHT
    }

    return result
}

fun Int.toAtlasNames(): Map<String, Boolean> {
    return mapOf(
        AtlasNames.CORE to true,
        AtlasNames.TOP to top(),
        AtlasNames.LEFT to left(),
        AtlasNames.BOTTOM to bottom(),
        AtlasNames.RIGHT to right(),
        AtlasNames.CORNER_TOP_LEFT to (!top() && !left()),
        AtlasNames.CORNER_TOP_RIGHT to (!top() && !right()),
        AtlasNames.CORNER_BOTTOM_LEFT to (!bottom() && !left()),
        AtlasNames.CORNER_BOTTOM_RIGHT to (!bottom() && !right()),
        AtlasNames.BORDER_CORNER_RIGHT to (top() && right() && !topRight()),
        AtlasNames.BORDER_CORNER_LEFT to (top() && left() && !topLeft()),
        AtlasNames.BORDER_CORNER_BOTTOM_RIGHT to (bottom() && right() && !bottomRight()),
        AtlasNames.BORDER_CORNER_BOTTOM_LEFT to (bottom() && left() && !bottomLeft()),
        AtlasNames.FILL_TOP_LEFT to (top() && left() && topLeft()),
        AtlasNames.FILL_TOP_RIGHT to (top() && right() && topRight()),
        AtlasNames.FILL_BOTTOM_LEFT to (bottom() && left() && bottomLeft()),
        AtlasNames.FILL_BOTTOM_RIGHT to (bottom() && right() && bottomRight()),
    )
}

const val AREA_NO_FORM = 0b00000000

const val AREA_FULL_FORM = 0b11111111
