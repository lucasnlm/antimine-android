package dev.lucasnlm.antimine.core.themes.model

import androidx.annotation.StyleRes
import dev.lucasnlm.antimine.common.level.models.AreaPalette

data class AppTheme(
    val id: Long,
    @StyleRes val theme: Int,
    @StyleRes val themeNoActionBar: Int,
    val palette: AreaPalette
)
