package dev.lucasnlm.antimine.control.model

import androidx.annotation.StringRes
import dev.lucasnlm.antimine.core.control.ControlStyle

data class ControlDetails(
    val id: Long,
    val controlStyle: ControlStyle,
    @StringRes val titleId: Int,
    @StringRes val firstActionId: Int,
    @StringRes val firstActionResponseId: Int,
    @StringRes val secondActionId: Int,
    @StringRes val secondActionResponseId: Int
)
