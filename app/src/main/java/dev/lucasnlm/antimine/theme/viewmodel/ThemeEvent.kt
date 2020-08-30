package dev.lucasnlm.antimine.theme.viewmodel

import dev.lucasnlm.antimine.core.themes.model.AppTheme

sealed class ThemeEvent {
    data class Unlock(
        val themeId: Long
    ) : ThemeEvent()

    data class ChangeTheme(
        val newTheme: AppTheme,
    ) : ThemeEvent()

    object ResetTheme : ThemeEvent()
}
