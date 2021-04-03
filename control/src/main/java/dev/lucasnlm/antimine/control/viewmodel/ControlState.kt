package dev.lucasnlm.antimine.control.viewmodel

import dev.lucasnlm.antimine.control.models.ControlDetails
import dev.lucasnlm.antimine.preferences.models.ControlStyle

data class ControlState(
    val longPress: Int,
    val touchSensibility: Int,
    val selected: ControlStyle,
    val controls: List<ControlDetails>,
)
