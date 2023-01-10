package dev.lucasnlm.antimine.ui.model

import androidx.annotation.DrawableRes

data class AppSkin(
    val id: Long,
    val file: String,
    @DrawableRes val imageRes: Int,
    val canTint: Boolean,
    val isPaid: Boolean,
    val joinAreas: Boolean,
    val background: Int = 0,
)
