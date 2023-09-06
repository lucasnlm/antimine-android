package dev.lucasnlm.antimine.control.viewmodel

import dev.lucasnlm.antimine.control.models.ControlDetails
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManager
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import kotlinx.coroutines.flow.flow
import dev.lucasnlm.antimine.i18n.R as i18n

class ControlViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val hapticFeedbackManager: HapticFeedbackManager,
) : IntentViewModel<ControlEvent, ControlState>() {

    private val gameControlOptions =
        listOf(
            ControlDetails(
                id = 0L,
                controlStyle = ControlStyle.Standard,
                firstActionId = i18n.string.single_click,
                firstActionResponseId = i18n.string.open_tile,
                secondActionId = i18n.string.long_press,
                secondActionResponseId = i18n.string.flag_tile,
            ),
            ControlDetails(
                id = 1L,
                controlStyle = ControlStyle.FastFlag,
                firstActionId = i18n.string.single_click,
                firstActionResponseId = i18n.string.flag_tile,
                secondActionId = i18n.string.long_press,
                secondActionResponseId = i18n.string.open_tile,
            ),
            ControlDetails(
                id = 2L,
                controlStyle = ControlStyle.DoubleClick,
                firstActionId = i18n.string.single_click,
                firstActionResponseId = i18n.string.flag_tile,
                secondActionId = i18n.string.double_click,
                secondActionResponseId = i18n.string.open_tile,
            ),
            ControlDetails(
                id = 3L,
                controlStyle = ControlStyle.DoubleClickInverted,
                firstActionId = i18n.string.single_click,
                firstActionResponseId = i18n.string.open_tile,
                secondActionId = i18n.string.double_click,
                secondActionResponseId = i18n.string.flag_tile,
            ),
            ControlDetails(
                id = 4L,
                controlStyle = ControlStyle.SwitchMarkOpen,
                firstActionId = i18n.string.switch_control_desc,
                firstActionResponseId = 0,
                secondActionId = 0,
                secondActionResponseId = 0,
            ),
        )

    private fun hasChangedPreferences(): Boolean {
        return preferencesRepository.hasControlCustomizations()
    }

    override fun initialState(): ControlState {
        val controlDetails =
            gameControlOptions.firstOrNull {
                it.controlStyle == preferencesRepository.controlStyle()
            }
        return ControlState(
            touchSensibility = preferencesRepository.touchSensibility(),
            longPress = preferencesRepository.customLongPressTimeout().toInt(),
            doubleClick = preferencesRepository.getDoubleClickTimeout().toInt(),
            selected = controlDetails?.controlStyle ?: ControlStyle.Standard,
            controls = gameControlOptions,
            hapticFeedbackLevel = preferencesRepository.getHapticFeedbackLevel(),
            showReset = hasChangedPreferences(),
        )
    }

    override suspend fun mapEventToState(event: ControlEvent) =
        flow {
            when (event) {
                is ControlEvent.UpdateHapticFeedbackLevel -> {
                    val value = event.value.coerceIn(0, MAX_HAPTIC_VALUE)
                    preferencesRepository.setHapticFeedbackLevel(value)
                    hapticFeedbackManager.longPressFeedback()
                    preferencesRepository.setHapticFeedback(value != 0)

                    val newState =
                        state.copy(
                            showReset = hasChangedPreferences(),
                        )
                    emit(newState)
                }
                is ControlEvent.UpdateDoubleClick -> {
                    preferencesRepository.setDoubleClickTimeout(event.value.toLong())

                    val newState =
                        state.copy(
                            doubleClick = event.value,
                            showReset = hasChangedPreferences(),
                        )
                    emit(newState)
                }
                is ControlEvent.UpdateLongPress -> {
                    preferencesRepository.setCustomLongPressTimeout(event.value.toLong())

                    val newState =
                        state.copy(
                            longPress = event.value,
                            showReset = hasChangedPreferences(),
                        )
                    emit(newState)
                }
                is ControlEvent.UpdateTouchSensibility -> {
                    preferencesRepository.setTouchSensibility(event.value)
                    val newState =
                        state.copy(
                            touchSensibility = event.value,
                            showReset = hasChangedPreferences(),
                        )
                    emit(newState)
                }
                is ControlEvent.Reset -> {
                    preferencesRepository.resetControls()

                    val newState =
                        state.copy(
                            longPress = preferencesRepository.customLongPressTimeout().toInt(),
                            touchSensibility = preferencesRepository.touchSensibility(),
                            doubleClick = preferencesRepository.getDoubleClickTimeout().toInt(),
                            showReset = hasChangedPreferences(),
                        )
                    emit(newState)
                }
                is ControlEvent.SelectControlStyle -> {
                    val controlStyle = event.controlStyle
                    preferencesRepository.useControlStyle(controlStyle)

                    val selected = state.controls.first { it.controlStyle == event.controlStyle }

                    val newState =
                        state.copy(
                            selected = selected.controlStyle,
                        )

                    emit(newState)
                }
            }
        }

    companion object {
        const val MAX_HAPTIC_VALUE = 200
    }
}
