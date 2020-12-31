package dev.lucasnlm.antimine.theme.viewmodel

import dev.lucasnlm.antimine.ui.model.AppTheme

data class ThemeState(
    val current: AppTheme,
    val themes: List<AppTheme>,
)
