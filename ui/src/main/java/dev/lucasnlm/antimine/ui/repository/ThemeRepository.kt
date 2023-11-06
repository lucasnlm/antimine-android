package dev.lucasnlm.antimine.ui.repository

import dev.lucasnlm.antimine.ui.model.AppSkin
import dev.lucasnlm.antimine.ui.model.AppTheme

interface ThemeRepository {
    fun getCustomTheme(): AppTheme?

    fun getSkin(): AppSkin

    fun getTheme(): AppTheme

    fun getAllThemes(): List<AppTheme>

    fun getAllDarkThemes(): List<AppTheme>

    fun getAllSkins(): List<AppSkin>

    fun setTheme(themeId: Long)

    fun setSkin(skinId: Long)

    fun reset(): AppTheme
}
