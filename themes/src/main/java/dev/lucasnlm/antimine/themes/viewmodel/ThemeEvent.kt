package dev.lucasnlm.antimine.themes.viewmodel

import dev.lucasnlm.antimine.ui.model.AppTheme

sealed class ThemeEvent {
    data class Unlock(
        val themeId: Long
    ) : ThemeEvent()

    data class ChangeTheme(
        val newTheme: AppTheme,
    ) : ThemeEvent()

    object ResetTheme : ThemeEvent()
}
