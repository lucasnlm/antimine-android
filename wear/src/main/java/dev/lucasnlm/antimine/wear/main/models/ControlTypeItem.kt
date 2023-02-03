package dev.lucasnlm.antimine.wear.main.models

import androidx.annotation.StringRes

data class ControlTypeItem(
    val id: Long,
    @StringRes val primaryAction: Int,
    @StringRes val secondaryAction: Int? = null,
    val selected: Boolean,
    val onClick: () -> Unit,
)
