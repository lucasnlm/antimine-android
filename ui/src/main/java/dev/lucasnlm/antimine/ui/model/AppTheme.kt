package dev.lucasnlm.antimine.ui.model

import androidx.annotation.StringRes
import androidx.annotation.StyleRes

/**
 * A class that represents a theme for the app.
 * @property id The id of the theme.
 * @property theme The resource id of the theme.
 * @property palette The palette of the theme.
 * @property isPremium Whether the theme is premium.
 * @property isDarkTheme Whether the theme is dark.
 * @property name The name of the theme.
 */
data class AppTheme(
    val id: Long,
    @StyleRes val theme: Int,
    val palette: AreaPalette,
    val isPremium: Boolean = true,
    val isDarkTheme: Boolean,
    @StringRes val name: Int? = null,
)
