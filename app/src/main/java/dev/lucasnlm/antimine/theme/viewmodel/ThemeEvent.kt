package dev.lucasnlm.antimine.theme.viewmodel

import dev.lucasnlm.antimine.core.themes.model.AppTheme

sealed class ThemeEvent {
    object Unlock : ThemeEvent()

    data class ChangeTheme(
        val newTheme: AppTheme
    ) : ThemeEvent()
}
