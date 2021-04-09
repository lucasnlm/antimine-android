package dev.lucasnlm.antimine.ui.model

import androidx.annotation.StyleRes

data class AppTheme(
    val id: Long,
    @StyleRes val theme: Int,
    val palette: AreaPalette,
    val isPaid: Boolean = true,
)
