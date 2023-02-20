package dev.lucasnlm.antimine.wear.main.models

import androidx.annotation.StringRes
import dev.lucasnlm.antimine.preferences.models.ControlStyle

data class ControlTypeItem(
    val id: Long,
    val controlStyle: ControlStyle,
    @StringRes val primaryAction: Int,
    @StringRes val secondaryAction: Int? = null,
)
