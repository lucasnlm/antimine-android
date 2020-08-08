package dev.lucasnlm.antimine.core.themes.repository

import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.common.level.models.AreaPalette
import dev.lucasnlm.antimine.core.themes.model.AppTheme

object Themes {
    val LightTheme = AppTheme(
        id = 1L,
        theme = R.style.CustomLightTheme,
        themeNoActionBar = R.style.CustomLightTheme_NoActionBar,
        palette = AreaPalette(
            border = 0x424242,
            background = 0xFFFFFF,
            covered = 0x424242,
            uncovered = 0xd5d2cc,
            minesAround1 = 0x527F8D,
            minesAround2 = 0x2B8D43,
            minesAround3 = 0xE65100,
            minesAround4 = 0x20A5f7,
            minesAround5 = 0xED1C24,
            minesAround6 = 0xFFC107,
            minesAround7 = 0x66126B,
            minesAround8 = 0x000000,
            highlight = 0x212121,
            focus = 0xD32F2F
        )
    )

    val DarkTheme = AppTheme(
        id = 2L,
        theme = R.style.CustomDarkTheme,
        themeNoActionBar = R.style.CustomDarkTheme_NoActionBar,
        palette = AreaPalette(
            border = 0x171717,
            background = 0x212121,
            covered = 0x171717,
            uncovered = 0x424242,
            minesAround1 = 0xd5d2cc,
            minesAround2 = 0xd5d2cc,
            minesAround3 = 0xd5d2cc,
            minesAround4 = 0xd5d2cc,
            minesAround5 = 0xd5d2cc,
            minesAround6 = 0xd5d2cc,
            minesAround7 = 0xd5d2cc,
            minesAround8 = 0xd5d2cc,
            highlight = 0xFFFFFF,
            focus = 0xFFFFFF
        )
    )
}
