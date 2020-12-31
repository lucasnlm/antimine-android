package dev.lucasnlm.antimine.tutorial.view

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark

object TutorialField {
    fun getStep0(): List<Area> {
        return (0..24).map {
            Area(it, 0, 0, isCovered = true)
        }.toList()
    }

    private fun getDefaultStep(): MutableList<Area> {
        return mutableListOf(
            Area(
                0, 0, 0, 0, hasMine = false,
                mistake = false, isCovered = false, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                1, 1, 0, 0, hasMine = false,
                mistake = false, isCovered = false, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                2, 2, 0, 0, hasMine = false,
                mistake = false, isCovered = false, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                3, 3, 0, 2, hasMine = false,
                mistake = false, isCovered = false, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                4, 4, 0, 0, hasMine = true,
                mistake = false, isCovered = true, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                5, 0, 1, 1, hasMine = false,
                mistake = false, isCovered = false, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                6, 1, 1, 1, hasMine = false,
                mistake = false, isCovered = false, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                7, 2, 1, 0, hasMine = false,
                mistake = false, isCovered = false, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                8, 3, 1, 2, hasMine = false,
                mistake = false, isCovered = false, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                9, 4, 1, 0, hasMine = true,
                mistake = false, isCovered = true, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                10, 0, 2, 0, hasMine = true,
                mistake = false, isCovered = true, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                11, 1, 2, 1, hasMine = false,
                mistake = false, isCovered = false, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                12, 2, 2, 0, hasMine = false,
                mistake = false, isCovered = false, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                13, 3, 2, 1, hasMine = false,
                mistake = false, isCovered = false, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                14, 4, 2, 1, hasMine = false,
                mistake = false, isCovered = true, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                15, 0, 3, 1, hasMine = false,
                mistake = false, isCovered = true, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                16, 1, 3, 2, hasMine = false,
                mistake = false, isCovered = false, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                17, 2, 3, 1, hasMine = false,
                mistake = false, isCovered = false, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                18, 3, 3, 1, hasMine = false,
                mistake = false, isCovered = false, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                19, 4, 3, 0, hasMine = false,
                mistake = false, isCovered = true, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                20, 0, 4, 0, hasMine = false,
                mistake = false, isCovered = true, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                21, 1, 4, 1, hasMine = false,
                mistake = false, isCovered = true, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                22, 2, 4, 0, hasMine = true,
                mistake = false, isCovered = true, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                23, 3, 4, 1, hasMine = false,
                mistake = false, isCovered = true, mark = Mark.None,
                highlighted = false,
            ),
            Area(
                24, 4, 4, 0, hasMine = false,
                mistake = false, isCovered = true, mark = Mark.None,
                highlighted = false,
            ),
        )
    }

    fun getStep1(): List<Area> {
        return getDefaultStep().apply {
            this[6] = this[6].copy(highlighted = true)
            this[10] = this[10].copy(highlighted = true)
        }
    }

    fun getStep2(): List<Area> {
        return getDefaultStep().apply {
            this[10] = this[10].copy(mark = Mark.Flag)
            this[11] = this[11].copy(highlighted = true)
            this[15] = this[15].copy(highlighted = true)
        }
    }

    fun getStep3(): List<Area> {
        return getDefaultStep().apply {
            this[10] = this[10].copy(mark = Mark.Flag)
            this[15] = this[15].copy(highlighted = true, isCovered = false)
            this[20] = this[20].copy(highlighted = true)
            this[21] = this[21].copy(highlighted = true)
        }
    }

    fun getStep4(): List<Area> {
        return getDefaultStep().apply {
            this[10] = this[10].copy(mark = Mark.Flag)
            this[15] = this[15].copy(isCovered = false)
            this[20] = this[20].copy(isCovered = false)
            this[21] = this[21].copy(isCovered = false)
            this[16] = this[16].copy(highlighted = true)
            this[22] = this[22].copy(highlighted = true)
        }
    }

    fun getStep5(): List<Area> {
        return getDefaultStep().apply {
            this[10] = this[10].copy(mark = Mark.Flag)
            this[15] = this[15].copy(isCovered = false)
            this[20] = this[20].copy(isCovered = false)
            this[21] = this[21].copy(isCovered = false)
            this[17] = this[17].copy(highlighted = true)
            this[23] = this[23].copy(highlighted = true)
            this[22] = this[22].copy(mark = Mark.Flag)
        }
    }

    fun getStep6(): List<Area> {
        return getDefaultStep().apply {
            this[10] = this[10].copy(mark = Mark.Flag)
            this[15] = this[15].copy(isCovered = false)
            this[20] = this[20].copy(isCovered = false)
            this[21] = this[21].copy(isCovered = false)
            this[23] = this[23].copy(isCovered = false)
            this[22] = this[22].copy(mark = Mark.Flag)
            this[18] = this[18].copy(highlighted = true)

            this[24] = this[24].copy(highlighted = true)
            this[19] = this[19].copy(highlighted = true)
            this[14] = this[14].copy(highlighted = true)
        }
    }

    fun getStep7(): List<Area> {
        return getDefaultStep().apply {
            this[10] = this[10].copy(mark = Mark.Flag)
            this[15] = this[15].copy(isCovered = false)
            this[20] = this[20].copy(isCovered = false)
            this[21] = this[21].copy(isCovered = false)
            this[23] = this[23].copy(isCovered = false)
            this[22] = this[22].copy(mark = Mark.Flag)
            this[24] = this[24].copy(isCovered = false)
            this[19] = this[19].copy(isCovered = false)
            this[14] = this[14].copy(isCovered = false)
            this[13] = this[13].copy(highlighted = true)
            this[9] = this[9].copy(highlighted = true)
        }
    }

    fun getStep8(): List<Area> {
        return getDefaultStep().apply {
            this[10] = this[10].copy(mark = Mark.Flag)
            this[15] = this[15].copy(isCovered = false)
            this[20] = this[20].copy(isCovered = false)
            this[21] = this[21].copy(isCovered = false)
            this[23] = this[23].copy(isCovered = false)
            this[22] = this[22].copy(mark = Mark.Flag)
            this[24] = this[24].copy(isCovered = false)
            this[19] = this[19].copy(isCovered = false)
            this[14] = this[14].copy(isCovered = false)
            this[8] = this[8].copy(highlighted = true)
            this[4] = this[4].copy(highlighted = true)
            this[9] = this[9].copy(mark = Mark.Flag)
        }
    }

    fun getStep9(): List<Area> {
        return getDefaultStep().apply {
            this[10] = this[10].copy(mark = Mark.Flag)
            this[15] = this[15].copy(isCovered = false)
            this[20] = this[20].copy(isCovered = false)
            this[21] = this[21].copy(isCovered = false)
            this[23] = this[23].copy(isCovered = false)
            this[22] = this[22].copy(mark = Mark.Flag)
            this[24] = this[24].copy(isCovered = false)
            this[19] = this[19].copy(isCovered = false)
            this[14] = this[14].copy(isCovered = false)
            this[4] = this[4].copy(mark = Mark.Flag)
            this[9] = this[9].copy(mark = Mark.Flag)
        }
    }
}
