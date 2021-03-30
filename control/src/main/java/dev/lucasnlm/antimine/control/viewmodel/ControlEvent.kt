package dev.lucasnlm.antimine.control.viewmodel

import dev.lucasnlm.antimine.preferences.models.ControlStyle

sealed class ControlEvent {
    data class SelectControlStyle(
        val controlStyle: ControlStyle,
    ) : ControlEvent()

    object Reset : ControlEvent()

    data class UpdateTouchSensibility(
        val value: Int,
    ) : ControlEvent()

    data class UpdateLongPress(
        val value: Int,
    ) : ControlEvent()
}
