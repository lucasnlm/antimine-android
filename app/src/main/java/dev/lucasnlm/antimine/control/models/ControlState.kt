package dev.lucasnlm.antimine.control.models

data class ControlState(
    val selectedId: Int,
    val gameControls: List<ControlDetails>
)
