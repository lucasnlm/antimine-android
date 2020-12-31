package dev.lucasnlm.antimine.themes.viewmodel

import dev.lucasnlm.antimine.ui.model.AppTheme

data class ThemeState(
    val current: AppTheme,
    val themes: List<AppTheme>,
)
