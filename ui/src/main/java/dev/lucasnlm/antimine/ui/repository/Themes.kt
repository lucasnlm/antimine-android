package dev.lucasnlm.antimine.ui.repository

import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.model.AreaPalette

object Themes {
    const val WHITE = 0xFFFFFF

    fun lightTheme() =
        AppTheme(
            id = 1L,
            theme = R.style.CustomLightTheme,
            palette =
                AreaPalette(
                    accent = 0xD32F2F,
                    background = 0xFFFFFF,
                    covered = 0x424242,
                    coveredOdd = 0x424242,
                    uncovered = 0xd5d2cc,
                    uncoveredOdd = 0xd5d2cc,
                    minesAround1 = 0x527F8D,
                    minesAround2 = 0x2B8D43,
                    minesAround3 = 0xE65100,
                    minesAround4 = 0x20A5f7,
                    minesAround5 = 0xED1C24,
                    minesAround6 = 0xFFC107,
                    minesAround7 = 0x66126B,
                    minesAround8 = 0x000000,
                    highlight = 0x212121,
                    focus = 0xD32F2F,
                ),
            isPremium = true,
            isDarkTheme = false,
        )

    fun darkTheme() =
        AppTheme(
            id = 3L,
            theme = R.style.CustomDarkTheme,
            palette =
                AreaPalette(
                    accent = 0xFFFFFF,
                    background = 0x212121,
                    covered = 0xd5d2cc,
                    coveredOdd = 0xd5d2cc,
                    uncovered = 0x424242,
                    uncoveredOdd = 0x424242,
                    minesAround1 = 0xd5d2cc,
                    minesAround2 = 0xd5d2cc,
                    minesAround3 = 0xd5d2cc,
                    minesAround4 = 0xd5d2cc,
                    minesAround5 = 0xd5d2cc,
                    minesAround6 = 0xd5d2cc,
                    minesAround7 = 0xd5d2cc,
                    minesAround8 = 0xd5d2cc,
                    highlight = 0xFFFFFF,
                    focus = 0xFFFFFF,
                ),
            isPremium = true,
            isDarkTheme = true,
        )

    private fun amoledTheme() =
        AppTheme(
            id = 2L,
            theme = R.style.CustomAmoledTheme,
            palette =
                AreaPalette(
                    accent = 0xFFFFFF,
                    background = 0x000000,
                    covered = 0x616161,
                    coveredOdd = 0x616161,
                    uncovered = 0x000000,
                    uncoveredOdd = 0x050505,
                    minesAround1 = 0xCCCCCC,
                    minesAround2 = 0xFFFFFF,
                    minesAround3 = 0xDDDDDD,
                    minesAround4 = 0xCCCCCC,
                    minesAround5 = 0xDDDDDD,
                    minesAround6 = 0xFFFFFF,
                    minesAround7 = 0xCCCCCC,
                    minesAround8 = 0xCCCCCC,
                    highlight = 0x212121,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = true,
        )

    private fun amoledTheme2() =
        AppTheme(
            id = 22L,
            theme = R.style.CustomAmoledTheme,
            palette =
                AreaPalette(
                    accent = 0xFFFFFF,
                    background = 0x000000,
                    covered = 0xEEEEEE,
                    coveredOdd = 0xDDDDDD,
                    uncovered = 0x000000,
                    uncoveredOdd = 0x050505,
                    minesAround1 = 0xCCCCCC,
                    minesAround2 = 0xFFFFFF,
                    minesAround3 = 0xDDDDDD,
                    minesAround4 = 0xCCCCCC,
                    minesAround5 = 0xDDDDDD,
                    minesAround6 = 0xFFFFFF,
                    minesAround7 = 0xCCCCCC,
                    minesAround8 = 0xCCCCCC,
                    highlight = 0x212121,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = true,
        )

    private fun standardChessTheme() =
        AppTheme(
            id = 5L,
            theme = R.style.CustomLightTheme,
            palette =
                AreaPalette(
                    accent = 0x37474f,
                    background = 0xFFFFFF,
                    covered = 0x4a4a4a,
                    coveredOdd = 0x383838,
                    uncovered = 0xe2e1da,
                    uncoveredOdd = 0xd5d2cc,
                    minesAround1 = 0x527F8D,
                    minesAround2 = 0x2B8D43,
                    minesAround3 = 0xE65100,
                    minesAround4 = 0x20A5f7,
                    minesAround5 = 0xED1C24,
                    minesAround6 = 0xFFC107,
                    minesAround7 = 0x66126B,
                    minesAround8 = 0x000000,
                    highlight = 0x212121,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    private fun goldenTheme() =
        AppTheme(
            id = 23L,
            theme = R.style.CustomLightTheme,
            palette =
                AreaPalette(
                    accent = 0x37474f,
                    background = 0xFFFFFF,
                    covered = 0xf9a825,
                    coveredOdd = 0xf9a825,
                    uncovered = 0xe2e1da,
                    uncoveredOdd = 0xd5d2cc,
                    minesAround1 = 0x527F8D,
                    minesAround2 = 0x2B8D43,
                    minesAround3 = 0xE65100,
                    minesAround4 = 0x20A5f7,
                    minesAround5 = 0xED1C24,
                    minesAround6 = 0xFFC107,
                    minesAround7 = 0x66126B,
                    minesAround8 = 0x000000,
                    highlight = 0x212121,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    private fun blueTheme() =
        AppTheme(
            id = 24L,
            theme = R.style.CustomMarineTheme,
            palette =
                AreaPalette(
                    accent = 0x37474f,
                    background = 0xFFFFFF,
                    covered = 0x0277bd,
                    coveredOdd = 0x0277bd,
                    uncovered = 0xe2e1da,
                    uncoveredOdd = 0xd5d2cc,
                    minesAround1 = 0x527F8D,
                    minesAround2 = 0x2B8D43,
                    minesAround3 = 0xE65100,
                    minesAround4 = 0x20A5f7,
                    minesAround5 = 0xED1C24,
                    minesAround6 = 0xFFC107,
                    minesAround7 = 0x66126B,
                    minesAround8 = 0x000000,
                    highlight = 0x212121,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    private fun gardenTheme() =
        AppTheme(
            id = 4L,
            theme = R.style.CustomGardenTheme,
            palette =
                AreaPalette(
                    accent = 0x689f38,
                    background = 0xefebe9,
                    covered = 0x689f38,
                    coveredOdd = 0x558b2f,
                    uncovered = 0xefebe9,
                    uncoveredOdd = 0xd7ccc8,
                    minesAround1 = 0x527F8D,
                    minesAround2 = 0x2B8D43,
                    minesAround3 = 0xE65100,
                    minesAround4 = 0x20A5f7,
                    minesAround5 = 0xED1C24,
                    minesAround6 = 0xFFC107,
                    minesAround7 = 0x66126B,
                    minesAround8 = 0x000000,
                    highlight = 0x689f38,
                    focus = 0xFFFFFF,
                ),
            isDarkTheme = false,
        )

    private fun marineTheme() =
        AppTheme(
            id = 6L,
            theme = R.style.CustomMarineTheme,
            palette =
                AreaPalette(
                    accent = 0x0277bd,
                    background = 0xFFFFFF,
                    covered = 0x0277bd,
                    coveredOdd = 0x006aa8,
                    uncovered = 0xc0d2d9,
                    uncoveredOdd = 0xc0d2d9,
                    minesAround1 = 0x527F8D,
                    minesAround2 = 0x2B8D43,
                    minesAround3 = 0xE65100,
                    minesAround4 = 0x20A5f7,
                    minesAround5 = 0xED1C24,
                    minesAround6 = 0xFFC107,
                    minesAround7 = 0x66126B,
                    minesAround8 = 0x000000,
                    highlight = 0x212121,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    private fun blueGreyTheme() =
        AppTheme(
            id = 7L,
            theme = R.style.CustomBlueGreyTheme,
            palette =
                AreaPalette(
                    accent = 0x37474f,
                    background = 0xFFFFFF,
                    covered = 0x37474f,
                    coveredOdd = 0x37474f,
                    uncovered = 0xcfd8dc,
                    uncoveredOdd = 0xcfd8dc,
                    minesAround1 = 0x527F8D,
                    minesAround2 = 0x2B8D43,
                    minesAround3 = 0x546e7a,
                    minesAround4 = 0x20A5f7,
                    minesAround5 = 0xED1C24,
                    minesAround6 = 0xFFC107,
                    minesAround7 = 0x66126B,
                    minesAround8 = 0x000000,
                    highlight = 0x212121,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    private fun darkBlueGreyTheme() =
        AppTheme(
            id = 25L,
            theme = R.style.CustomBlueGreyTheme,
            palette =
                AreaPalette(
                    accent = 0x37474f,
                    background = 0x677075,
                    covered = 0x071821,
                    coveredOdd = 0x071821,
                    uncovered = 0xcfd8dc,
                    uncoveredOdd = 0xcfd8dc,
                    minesAround1 = 0xFFFFFF,
                    minesAround2 = 0xCCCCCC,
                    minesAround3 = 0xAAAAAA,
                    minesAround4 = 0xDDDDDD,
                    minesAround5 = 0xFFFFFF,
                    minesAround6 = 0xFF0000,
                    minesAround7 = 0xFF0000,
                    minesAround8 = 0xFF0000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    private fun darkOrangeTheme() =
        AppTheme(
            id = 8L,
            theme = R.style.CustomOrangeTheme,
            palette =
                AreaPalette(
                    accent = 0xfb8c00,
                    background = 0x212121,
                    covered = 0xfb8c00,
                    coveredOdd = 0xfb8c00,
                    uncovered = 0x303030,
                    uncoveredOdd = 0x252525,
                    minesAround1 = 0xDDDDDD,
                    minesAround2 = 0xEEEEEE,
                    minesAround3 = 0xCCCCCC,
                    minesAround4 = 0xBBBBBB,
                    minesAround5 = 0xAAAAAA,
                    minesAround6 = 0xFFFFFF,
                    minesAround7 = 0xBBBBBB,
                    minesAround8 = 0xEEEEEE,
                    highlight = 0xfb8c00,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = true,
        )

    private fun darkLimeTheme() =
        AppTheme(
            id = 16L,
            theme = R.style.CustomLimeTheme,
            palette =
                AreaPalette(
                    accent = 0xcddc39,
                    background = 0x212121,
                    covered = 0xcddc39,
                    coveredOdd = 0xcddc39,
                    uncovered = 0x212121,
                    uncoveredOdd = 0x1c1c1c,
                    minesAround1 = 0xFFFFFF,
                    minesAround2 = 0xCCCCCC,
                    minesAround3 = 0xAAAAAA,
                    minesAround4 = 0xDDDDDD,
                    minesAround5 = 0xFFFFFF,
                    minesAround6 = 0xFF0000,
                    minesAround7 = 0xFF0000,
                    minesAround8 = 0xFF0000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = true,
        )

    private fun darkYellowTheme() =
        AppTheme(
            id = 18L,
            theme = R.style.BananaTheme,
            palette =
                AreaPalette(
                    accent = 0xffeb3b,
                    background = 0x212121,
                    covered = 0xffeb3b,
                    coveredOdd = 0xe6d335,
                    uncovered = 0x212121,
                    uncoveredOdd = 0x1c1c1c,
                    minesAround1 = 0xFFFFFF,
                    minesAround2 = 0xCCCCCC,
                    minesAround3 = 0xAAAAAA,
                    minesAround4 = 0xDDDDDD,
                    minesAround5 = 0xFFFFFF,
                    minesAround6 = 0xFF0000,
                    minesAround7 = 0xFF0000,
                    minesAround8 = 0xFF0000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = true,
        )

    private fun pinkTheme() =
        AppTheme(
            id = 9L,
            theme = R.style.CustomPinkTheme,
            palette =
                AreaPalette(
                    accent = 0xf48fb1,
                    background = 0xFFFFFF,
                    covered = 0xf48fb1,
                    coveredOdd = 0xf48fb1,
                    uncovered = 0xfce4ec,
                    uncoveredOdd = 0xfce4ec,
                    minesAround1 = 0x616161,
                    minesAround2 = 0xe64a19,
                    minesAround3 = 0x8e24aa,
                    minesAround4 = 0x000000,
                    minesAround5 = 0x1e88e5,
                    minesAround6 = 0x424242,
                    minesAround7 = 0x616161,
                    minesAround8 = 0x000000,
                    highlight = 0x212121,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    private fun darkPinkTheme() =
        AppTheme(
            id = 26L,
            theme = R.style.CustomDarkPinkTheme,
            palette =
                AreaPalette(
                    accent = 0xf48fb1,
                    background = 0x212121,
                    covered = 0xf48fb1,
                    coveredOdd = 0xf48fb1,
                    uncovered = 0xfce4ec,
                    uncoveredOdd = 0xfce4ec,
                    minesAround1 = 0xFFFFFF,
                    minesAround2 = 0xCCCCCC,
                    minesAround3 = 0xAAAAAA,
                    minesAround4 = 0xDDDDDD,
                    minesAround5 = 0xFFFFFF,
                    minesAround6 = 0xFF0000,
                    minesAround7 = 0xFF0000,
                    minesAround8 = 0xFF0000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = true,
        )

    private fun purpleTheme() =
        AppTheme(
            id = 10L,
            theme = R.style.CustomPurpleTheme,
            palette =
                AreaPalette(
                    accent = 0x6a1b9a,
                    background = 0xFFFFFF,
                    covered = 0x6a1b9a,
                    coveredOdd = 0x6a1b9a,
                    uncovered = 0xd1c4e9,
                    uncoveredOdd = 0xd1c4e9,
                    minesAround1 = 0x616161,
                    minesAround2 = 0xe64a19,
                    minesAround3 = 0x8e24aa,
                    minesAround4 = 0x000000,
                    minesAround5 = 0x1e88e5,
                    minesAround6 = 0x424242,
                    minesAround7 = 0x616161,
                    minesAround8 = 0x000000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    private fun darkPurpleTheme() =
        AppTheme(
            id = 27L,
            theme = R.style.DarkCustomPurpleTheme,
            palette =
                AreaPalette(
                    accent = 0x6a1b9a,
                    background = 0x212121,
                    covered = 0x6a1b9a,
                    coveredOdd = 0x6a1b9a,
                    uncovered = 0xd1c4e9,
                    uncoveredOdd = 0xd1c4e9,
                    minesAround1 = 0xFFFFFF,
                    minesAround2 = 0xCCCCCC,
                    minesAround3 = 0xAAAAAA,
                    minesAround4 = 0xDDDDDD,
                    minesAround5 = 0xFFFFFF,
                    minesAround6 = 0xFF0000,
                    minesAround7 = 0xFF0000,
                    minesAround8 = 0xFF0000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = true,
        )

    private fun brownTheme() =
        AppTheme(
            id = 11L,
            theme = R.style.CustomLightTheme,
            palette =
                AreaPalette(
                    accent = 0x3e2723,
                    background = 0xFFFFFF,
                    covered = 0x3e2723,
                    coveredOdd = 0x4e342e,
                    uncovered = 0xd7ccc8,
                    uncoveredOdd = 0xefebe9,
                    minesAround1 = 0x616161,
                    minesAround2 = 0xe64a19,
                    minesAround3 = 0x8e24aa,
                    minesAround4 = 0x000000,
                    minesAround5 = 0x1e88e5,
                    minesAround6 = 0x424242,
                    minesAround7 = 0x616161,
                    minesAround8 = 0x000000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    private fun redTheme() =
        AppTheme(
            id = 12L,
            theme = R.style.CustomLightTheme,
            palette =
                AreaPalette(
                    accent = 0xc62828,
                    background = 0xFFFFFF,
                    covered = 0xc62828,
                    coveredOdd = 0xb71c1c,
                    uncovered = 0xd7ccc8,
                    uncoveredOdd = 0xefebe9,
                    minesAround1 = 0x616161,
                    minesAround2 = 0xe64a19,
                    minesAround3 = 0x8e24aa,
                    minesAround4 = 0x000000,
                    minesAround5 = 0x1e88e5,
                    minesAround6 = 0x424242,
                    minesAround7 = 0x616161,
                    minesAround8 = 0x000000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    private fun wineTheme() =
        AppTheme(
            id = 13L,
            theme = R.style.CustomLightTheme,
            palette =
                AreaPalette(
                    accent = 0x880e4f,
                    background = 0xFFFFFF,
                    covered = 0x880e4f,
                    coveredOdd = 0x750b42,
                    uncovered = 0xd7ccc8,
                    uncoveredOdd = 0xefebe9,
                    minesAround1 = 0x616161,
                    minesAround2 = 0xe64a19,
                    minesAround3 = 0x8e24aa,
                    minesAround4 = 0x000000,
                    minesAround5 = 0x1e88e5,
                    minesAround6 = 0x424242,
                    minesAround7 = 0x616161,
                    minesAround8 = 0x000000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    private fun darkBlueTheme() =
        AppTheme(
            id = 14L,
            theme = R.style.CustomLightTheme,
            palette =
                AreaPalette(
                    accent = 0x0d47a1,
                    background = 0xFFFFFF,
                    covered = 0x0d47a1,
                    coveredOdd = 0x0a3984,
                    uncovered = 0xedf1f2,
                    uncoveredOdd = 0xdcdee0,
                    minesAround1 = 0x616161,
                    minesAround2 = 0xe64a19,
                    minesAround3 = 0x8e24aa,
                    minesAround4 = 0x000000,
                    minesAround5 = 0x1e88e5,
                    minesAround6 = 0x424242,
                    minesAround7 = 0x616161,
                    minesAround8 = 0x000000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    private fun darkWhiteTheme() =
        AppTheme(
            id = 15L,
            theme = R.style.CustomDarkTheme,
            palette =
                AreaPalette(
                    accent = 0xedf1f2,
                    background = 0x212121,
                    covered = 0xedf1f2,
                    coveredOdd = 0xdcdee0,
                    uncovered = 0x212121,
                    uncoveredOdd = 0x1c1c1c,
                    minesAround1 = 0xFFFFFF,
                    minesAround2 = 0xCCCCCC,
                    minesAround3 = 0xAAAAAA,
                    minesAround4 = 0xDDDDDD,
                    minesAround5 = 0xFFFFFF,
                    minesAround6 = 0xFF0000,
                    minesAround7 = 0xFF0000,
                    minesAround8 = 0xFF0000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = true,
        )

    private fun darkLightBlueTheme() =
        AppTheme(
            id = 28L,
            theme = R.style.DarkLightBlueTheme,
            palette =
                AreaPalette(
                    accent = 0x42a5f5,
                    background = 0x212121,
                    covered = 0x42a5f5,
                    coveredOdd = 0x42a5f5,
                    uncovered = 0x212121,
                    uncoveredOdd = 0x1c1c1c,
                    minesAround1 = 0xFFFFFF,
                    minesAround2 = 0xCCCCCC,
                    minesAround3 = 0xAAAAAA,
                    minesAround4 = 0xDDDDDD,
                    minesAround5 = 0xFFFFFF,
                    minesAround6 = 0xFF0000,
                    minesAround7 = 0xFF0000,
                    minesAround8 = 0xFF0000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = true,
        )

    private fun darkRedTheme() =
        AppTheme(
            id = 29L,
            theme = R.style.DarkRedTheme,
            palette =
                AreaPalette(
                    accent = 0xf44336,
                    background = 0x212121,
                    covered = 0xf44336,
                    coveredOdd = 0xf44336,
                    uncovered = 0x212121,
                    uncoveredOdd = 0x1c1c1c,
                    minesAround1 = 0xFFFFFF,
                    minesAround2 = 0xCCCCCC,
                    minesAround3 = 0xAAAAAA,
                    minesAround4 = 0xDDDDDD,
                    minesAround5 = 0xFFFFFF,
                    minesAround6 = 0xFF0000,
                    minesAround7 = 0xFF0000,
                    minesAround8 = 0xFF0000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = true,
        )

    private fun darkPurpleTheme2() =
        AppTheme(
            id = 30L,
            theme = R.style.DarkCustomPurpleTheme,
            palette =
                AreaPalette(
                    accent = 0x7e57c2,
                    background = 0x212121,
                    covered = 0x7e57c2,
                    coveredOdd = 0x7e57c2,
                    uncovered = 0xd1c4e9,
                    uncoveredOdd = 0xd1c4e9,
                    minesAround1 = 0xFFFFFF,
                    minesAround2 = 0xCCCCCC,
                    minesAround3 = 0xAAAAAA,
                    minesAround4 = 0xDDDDDD,
                    minesAround5 = 0xFFFFFF,
                    minesAround6 = 0xFF0000,
                    minesAround7 = 0xFF0000,
                    minesAround8 = 0xFF0000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = true,
        )

    private fun whiteYellowTheme() =
        AppTheme(
            id = 19L,
            theme = R.style.BananaThemeLight,
            palette =
                AreaPalette(
                    accent = 0xfbc02d,
                    background = 0xe0e0e0,
                    covered = 0xfbc02d,
                    coveredOdd = 0xfbc02d,
                    uncovered = 0xf4f0d8,
                    uncoveredOdd = 0xfff8e1,
                    minesAround1 = 0x616161,
                    minesAround2 = 0xe64a19,
                    minesAround3 = 0x8e24aa,
                    minesAround4 = 0x000000,
                    minesAround5 = 0x1e88e5,
                    minesAround6 = 0x424242,
                    minesAround7 = 0x616161,
                    minesAround8 = 0x000000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    private fun whiteOrangeTheme() =
        AppTheme(
            id = 20L,
            theme = R.style.BananaThemeLight,
            palette =
                AreaPalette(
                    accent = 0xf57c00,
                    background = 0xe0e0e0,
                    covered = 0xf57c00,
                    coveredOdd = 0xf57c00,
                    uncovered = 0xf4f0d8,
                    uncoveredOdd = 0xfff8e1,
                    minesAround1 = 0x616161,
                    minesAround2 = 0xe64a19,
                    minesAround3 = 0x8e24aa,
                    minesAround4 = 0x000000,
                    minesAround5 = 0x1e88e5,
                    minesAround6 = 0x424242,
                    minesAround7 = 0x616161,
                    minesAround8 = 0x000000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    private fun whiteDarkYellowTheme() =
        AppTheme(
            id = 21L,
            theme = R.style.BananaThemeLight,
            palette =
                AreaPalette(
                    accent = 0x827717,
                    background = 0xe0e0e0,
                    covered = 0x827717,
                    coveredOdd = 0x827717,
                    uncovered = 0xf4f0d8,
                    uncoveredOdd = 0xfff8e1,
                    minesAround1 = 0x616161,
                    minesAround2 = 0xe64a19,
                    minesAround3 = 0x8e24aa,
                    minesAround4 = 0x000000,
                    minesAround5 = 0x1e88e5,
                    minesAround6 = 0x424242,
                    minesAround7 = 0x616161,
                    minesAround8 = 0x000000,
                    highlight = 0xd1c4e9,
                    focus = 0xD32F2F,
                ),
            isDarkTheme = false,
        )

    fun getAllCustom() =
        listOf(
            lightTheme(),
            darkTheme(),
            amoledTheme(),
            amoledTheme2(),
            darkOrangeTheme(),
            darkLimeTheme(),
            darkYellowTheme(),
            darkPinkTheme(),
            darkPurpleTheme(),
            darkWhiteTheme(),
            darkLightBlueTheme(),
            darkRedTheme(),
            darkPurpleTheme2(),
            standardChessTheme(),
            goldenTheme(),
            blueTheme(),
            gardenTheme(),
            marineTheme(),
            blueGreyTheme(),
            darkBlueGreyTheme(),
            pinkTheme(),
            purpleTheme(),
            brownTheme(),
            redTheme(),
            wineTheme(),
            darkBlueTheme(),
            whiteYellowTheme(),
            whiteOrangeTheme(),
            whiteDarkYellowTheme(),
        )
}
