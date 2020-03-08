package dev.lucasnlm.antimine.common.level.model

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import dev.lucasnlm.antimine.common.R

data class AreaPalette(
    @ColorInt val border: Int,
    @ColorInt val covered: Int,
    @ColorInt val uncovered: Int,
    @ColorInt val minesAround1: Int,
    @ColorInt val minesAround2: Int,
    @ColorInt val minesAround3: Int,
    @ColorInt val minesAround4: Int,
    @ColorInt val minesAround5: Int,
    @ColorInt val minesAround6: Int,
    @ColorInt val minesAround7: Int,
    @ColorInt val minesAround8: Int,
    @ColorInt val highlight: Int,
    @ColorInt val focus: Int
) {
    companion object {
        private fun toArgb(color: Int): Int {
            return Color.argb(
                0xFF,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
        }

        fun fromLightTheme() =
            AreaPalette(
                border = toArgb(0x424242),
                covered = toArgb(0x424242),
                uncovered = toArgb(0xd5d2cc),
                minesAround1 = toArgb(0x527F8D),
                minesAround2 = toArgb(0x2B8D43),
                minesAround3 = toArgb(0xE65100),
                minesAround4 = toArgb(0x20A5f7),
                minesAround5 = toArgb(0xED1C24),
                minesAround6 = toArgb(0xFFC107),
                minesAround7 = toArgb(0x66126B),
                minesAround8 = toArgb(0x000000),
                highlight = toArgb(0x212121),
                focus = toArgb(0xD32F2F)
            )

        fun fromDefault(context: Context) =
            AreaPalette(
                border = ContextCompat.getColor(context, R.color.view_cover),
                covered = ContextCompat.getColor(context, R.color.view_cover),
                uncovered = ContextCompat.getColor(context, R.color.view_clean),
                minesAround1 = ContextCompat.getColor(context, R.color.mines_around_1),
                minesAround2 = ContextCompat.getColor(context, R.color.mines_around_2),
                minesAround3 = ContextCompat.getColor(context, R.color.mines_around_3),
                minesAround4 = ContextCompat.getColor(context, R.color.mines_around_4),
                minesAround5 = ContextCompat.getColor(context, R.color.mines_around_5),
                minesAround6 = ContextCompat.getColor(context, R.color.mines_around_6),
                minesAround7 = ContextCompat.getColor(context, R.color.mines_around_7),
                minesAround8 = ContextCompat.getColor(context, R.color.mines_around_8),
                highlight = ContextCompat.getColor(context, R.color.highlight),
                focus = ContextCompat.getColor(context, R.color.accent)
            )

        fun fromContrast(context: Context) =
            AreaPalette(
                border = ContextCompat.getColor(context, android.R.color.white),
                covered = ContextCompat.getColor(context, android.R.color.black),
                uncovered = ContextCompat.getColor(context, android.R.color.black),
                minesAround1 = ContextCompat.getColor(context, R.color.white),
                minesAround2 = ContextCompat.getColor(context, R.color.white),
                minesAround3 = ContextCompat.getColor(context, R.color.white),
                minesAround4 = ContextCompat.getColor(context, R.color.white),
                minesAround5 = ContextCompat.getColor(context, R.color.white),
                minesAround6 = ContextCompat.getColor(context, R.color.white),
                minesAround7 = ContextCompat.getColor(context, R.color.white),
                minesAround8 = ContextCompat.getColor(context, R.color.white),
                highlight = ContextCompat.getColor(context, R.color.white),
                focus = ContextCompat.getColor(context, R.color.white)
            )
    }
}
