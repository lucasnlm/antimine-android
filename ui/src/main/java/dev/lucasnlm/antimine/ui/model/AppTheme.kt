package dev.lucasnlm.antimine.ui.model

import androidx.annotation.StringRes
import androidx.annotation.StyleRes

data class AppTheme(
    val id: Long,
    @StyleRes val theme: Int,
    val palette: AreaPalette,
    val isPaid: Boolean = true,
    val isDarkTheme: Boolean,
    @StringRes val name: Int? = null,
)
