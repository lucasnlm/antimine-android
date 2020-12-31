package dev.lucasnlm.antimine.ui.model

import androidx.annotation.ColorInt

data class AreaPalette(
    @ColorInt val border: Int,
    @ColorInt val background: Int,
    @ColorInt val covered: Int,
    @ColorInt val coveredOdd: Int,
    @ColorInt val uncovered: Int,
    @ColorInt val uncoveredOdd: Int,
    @ColorInt val minesAround1: Int,
    @ColorInt val minesAround2: Int,
    @ColorInt val minesAround3: Int,
    @ColorInt val minesAround4: Int,
    @ColorInt val minesAround5: Int,
    @ColorInt val minesAround6: Int,
    @ColorInt val minesAround7: Int,
    @ColorInt val minesAround8: Int,
    @ColorInt val highlight: Int,
    @ColorInt val focus: Int,
)
