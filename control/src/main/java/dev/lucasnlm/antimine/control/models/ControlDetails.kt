package dev.lucasnlm.antimine.control.models

import androidx.annotation.StringRes
import dev.lucasnlm.antimine.preferences.models.ControlStyle

data class ControlDetails(
    val id: Long,
    val controlStyle: ControlStyle,
    @StringRes val firstActionId: Int,
    @StringRes val firstActionResponseId: Int,
    @StringRes val secondActionId: Int,
    @StringRes val secondActionResponseId: Int,
)
