package dev.lucasnlm.antimine.control.viewmodel

import dev.lucasnlm.antimine.control.models.ControlDetails
import dev.lucasnlm.antimine.preferences.models.ControlStyle

data class ControlState(
    val selectedIndex: Int,
    val selected: ControlStyle,
    val gameControls: List<ControlDetails>,
)
