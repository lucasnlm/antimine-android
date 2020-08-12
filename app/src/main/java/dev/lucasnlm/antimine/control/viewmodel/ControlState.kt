package dev.lucasnlm.antimine.control.viewmodel

import dev.lucasnlm.antimine.control.models.ControlDetails

data class ControlState(
    val selectedId: Int,
    val gameControls: List<ControlDetails>
)
