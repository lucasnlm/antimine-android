package dev.lucasnlm.antimine.themes.viewmodel

import dev.lucasnlm.antimine.ui.model.AppTheme

data class ThemeState(
    val current: AppTheme,
    val squareRadius: Int,
    val squareSize: Int,
    val squareDivider: Int,
    val themes: List<AppTheme>,
)
