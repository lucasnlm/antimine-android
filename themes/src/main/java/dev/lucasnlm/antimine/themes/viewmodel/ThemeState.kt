package dev.lucasnlm.antimine.themes.viewmodel

import dev.lucasnlm.antimine.ui.model.AppSkin
import dev.lucasnlm.antimine.ui.model.AppTheme

data class ThemeState(
    val currentTheme: AppTheme,
    val currentAppSkin: AppSkin,
    val themes: List<AppTheme>,
    val appSkins: List<AppSkin>,
)
