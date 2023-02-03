package dev.lucasnlm.antimine.wear.main.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class MenuItem(
    val id: Long,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    val highlight: Boolean = false,
    val onClick: () -> Unit,
)
