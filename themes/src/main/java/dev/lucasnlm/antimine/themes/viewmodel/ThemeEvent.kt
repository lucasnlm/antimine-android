package dev.lucasnlm.antimine.themes.viewmodel

import dev.lucasnlm.antimine.ui.model.AppSkin
import dev.lucasnlm.antimine.ui.model.AppTheme

sealed class ThemeEvent {
    data class Unlock(
        val themeId: Long,
    ) : ThemeEvent()

    data class ChangeTheme(
        val newTheme: AppTheme,
    ) : ThemeEvent()

    data class ChangeSkin(
        val newSkin: AppSkin,
    ) : ThemeEvent()
}
