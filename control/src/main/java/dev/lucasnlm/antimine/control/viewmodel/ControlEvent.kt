package dev.lucasnlm.antimine.control.viewmodel

import dev.lucasnlm.antimine.preferences.models.ControlStyle

sealed class ControlEvent {
    data class SelectControlStyle(
        val controlStyle: ControlStyle,
    ) : ControlEvent()

    data object Reset : ControlEvent()

    data class UpdateTouchSensibility(
        val value: Int,
    ) : ControlEvent()

    data class UpdateLongPress(
        val value: Int,
    ) : ControlEvent()

    data class UpdateDoubleClick(
        val value: Int,
    ) : ControlEvent()

    data class UpdateHapticFeedbackLevel(
        val value: Int,
    ) : ControlEvent()
}
