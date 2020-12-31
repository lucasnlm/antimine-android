package dev.lucasnlm.antimine.themes.view

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark

object ExampleField {
    fun getField() = listOf(
        Area(
            0,
            0,
            0,
            1,
            hasMine = false,
            mistake = false,
            mark = Mark.None,
            isCovered = false
        ),
        Area(
            1,
            1,
            0,
            0,
            hasMine = true,
            mistake = false,
            mark = Mark.None,
            isCovered = false
        ),
        Area(
            2,
            2,
            0,
            0,
            hasMine = true,
            mistake = false,
            mark = Mark.None,
            isCovered = true
        ),
        Area(
            3,
            0,
            1,
            2,
            hasMine = false,
            mistake = false,
            mark = Mark.None,
            isCovered = false
        ),
        Area(
            4,
            1,
            1,
            3,
            hasMine = false,
            mistake = false,
            mark = Mark.None,
            isCovered = false
        ),
        Area(
            5,
            2,
            1,
            3,
            hasMine = true,
            mistake = false,
            mark = Mark.Flag,
            isCovered = true
        ),
        Area(
            6,
            0,
            2,
            0,
            hasMine = true,
            mistake = false,
            mark = Mark.Question,
            isCovered = true
        ),
        Area(
            7,
            1,
            2,
            4,
            hasMine = false,
            mistake = false,
            mark = Mark.None,
            isCovered = false
        ),
        Area(
            8, 2, 2, 0,
            hasMine = false, mistake = false, mark = Mark.None, isCovered = true, revealed = true
        ),
    )
}
