package dev.lucasnlm.antimine.gdx.actors

import androidx.annotation.VisibleForTesting
import dev.lucasnlm.antimine.gdx.AtlasNames

object AreaForm {
    const val TOP = 0b00000001
    const val BOTTOM = 0b00000010
    const val LEFT = 0b00000100
    const val RIGHT = 0b00001000
    const val TOP_LEFT = 0b00010000
    const val TOP_RIGHT = 0b00100000
    const val BOTTOM_LEFT = 0b01000000
    const val BOTTOM_RIGHT = 0b10000000
    const val ALL = 0b11111111

    private fun Int.checkForm(flag: Int): Boolean {
        return (this and flag) != 0
    }

    private fun Int.top(): Boolean = checkForm(TOP)

    private fun Int.bottom(): Boolean = checkForm(BOTTOM)

    private fun Int.left(): Boolean = checkForm(LEFT)

    private fun Int.right(): Boolean = checkForm(RIGHT)

    private fun Int.topLeft(): Boolean = checkForm(TOP_LEFT)

    private fun Int.topRight(): Boolean = checkForm(TOP_RIGHT)

    private fun Int.bottomLeft(): Boolean = checkForm(BOTTOM_LEFT)

    private fun Int.bottomRight(): Boolean = checkForm(BOTTOM_RIGHT)

    @VisibleForTesting
    fun Int.all(): Boolean {
        return (this and ALL) == ALL
    }

    @VisibleForTesting
    fun Int.none(): Boolean {
        return (this and ALL) == 0
    }

    @VisibleForTesting
    fun Int.allBut(vararg flags: Int): Boolean {
        var notFlags = 0
        flags.forEach { notFlags = notFlags or it }
        return checkForm(ALL and notFlags.inv()) && (notFlags and this) == 0
    }

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
            result = result or TOP
        }

        if (bottom) {
            result = result or BOTTOM
        }

        if (left) {
            result = result or LEFT
        }

        if (right) {
            result = result or RIGHT
        }

        if (topLeft) {
            result = result or TOP_LEFT
        }

        if (topRight) {
            result = result or TOP_RIGHT
        }

        if (bottomLeft) {
            result = result or BOTTOM_LEFT
        }

        if (bottomRight) {
            result = result or BOTTOM_RIGHT
        }

        return result
    }

    fun Int.toAtlasNames(): String? {
        if (all()) {
            return AtlasNames.TILE_FULL
        }

        if (none()) {
            return AtlasNames.TILE_NONE
        }

        if (allBut(TOP, RIGHT)) {
            return AtlasNames.TILE_CORNER_TR
        }

        if (allBut(TOP, LEFT)) {
            return AtlasNames.TILE_CORNER_TL
        }

        if (allBut(LEFT, BOTTOM)) {
            return AtlasNames.TILE_CORNER_BL
        }

        if (allBut(RIGHT, BOTTOM)) {
            return AtlasNames.TILE_CORNER_BR
        }

        if (allBut(BOTTOM)) {
            return AtlasNames.TILE_SIDE_BOTTOM
        }

        if (allBut(TOP)) {
            return AtlasNames.TILE_SIDE_TOP
        }

        if (allBut(LEFT)) {
            return AtlasNames.TILE_SIDE_LEFT
        }

        if (allBut(RIGHT)) {
            return AtlasNames.TILE_SIDE_RIGHT
        }

        return null

//        return mapOf(
//            AtlasNames.CORE to true,
//            AtlasNames.TOP to top(),
//            AtlasNames.LEFT to left(),
//            AtlasNames.BOTTOM to bottom(),
//            AtlasNames.RIGHT to right(),
//            AtlasNames.CORNER_TOP_LEFT to (!top() && !left()),
//            AtlasNames.CORNER_TOP_RIGHT to (!top() && !right()),
//            AtlasNames.CORNER_BOTTOM_LEFT to (!bottom() && !left()),
//            AtlasNames.CORNER_BOTTOM_RIGHT to (!bottom() && !right()),
//            AtlasNames.BORDER_CORNER_RIGHT to (top() && right() && !topRight()),
//            AtlasNames.BORDER_CORNER_LEFT to (top() && left() && !topLeft()),
//            AtlasNames.BORDER_CORNER_BOTTOM_RIGHT to (bottom() && right() && !bottomRight()),
//            AtlasNames.BORDER_CORNER_BOTTOM_LEFT to (bottom() && left() && !bottomLeft()),
//            AtlasNames.FILL_TOP_LEFT to (top() && left() && topLeft()),
//            AtlasNames.FILL_TOP_RIGHT to (top() && right() && topRight()),
//            AtlasNames.FILL_BOTTOM_LEFT to (bottom() && left() && bottomLeft()),
//            AtlasNames.FILL_BOTTOM_RIGHT to (bottom() && right() && bottomRight()),
//        ).filter {
//            it.value
//        }.keys
    }

    const val AREA_NO_FORM = 0b00000000

    const val AREA_FULL_FORM = 0b11111111
}
