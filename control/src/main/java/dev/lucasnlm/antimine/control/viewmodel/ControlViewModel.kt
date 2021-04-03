package dev.lucasnlm.antimine.control.viewmodel

import dev.lucasnlm.antimine.control.R
import dev.lucasnlm.antimine.control.models.ControlDetails
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import kotlinx.coroutines.flow.flow

class ControlViewModel(
    private val preferencesRepository: IPreferencesRepository,
) : IntentViewModel<ControlEvent, ControlState>() {

    private val gameControlOptions = listOf(
        ControlDetails(
            id = 0L,
            controlStyle = ControlStyle.Standard,
            firstActionId = R.string.single_click,
            firstActionResponseId = R.string.open_tile,
            secondActionId = R.string.long_press,
            secondActionResponseId = R.string.flag_tile,
        ),
        ControlDetails(
            id = 1L,
            controlStyle = ControlStyle.FastFlag,
            firstActionId = R.string.single_click,
            firstActionResponseId = R.string.flag_tile,
            secondActionId = R.string.long_press,
            secondActionResponseId = R.string.open_tile,
        ),
        ControlDetails(
            id = 2L,
            controlStyle = ControlStyle.DoubleClick,
            firstActionId = R.string.single_click,
            firstActionResponseId = R.string.flag_tile,
            secondActionId = R.string.double_click,
            secondActionResponseId = R.string.open_tile,
        ),
        ControlDetails(
            id = 3L,
            controlStyle = ControlStyle.DoubleClickInverted,
            firstActionId = R.string.single_click,
            firstActionResponseId = R.string.open_tile,
            secondActionId = R.string.double_click,
            secondActionResponseId = R.string.flag_tile,
        ),
        ControlDetails(
            id = 4L,
            controlStyle = ControlStyle.SwitchMarkOpen,
            firstActionId = R.string.switch_control_desc,
            firstActionResponseId = 0,
            secondActionId = 0,
            secondActionResponseId = 0,
        )
    )

    override fun initialState(): ControlState {
        val controlDetails = gameControlOptions.firstOrNull {
            it.controlStyle == preferencesRepository.controlStyle()
        }
        return ControlState(
            touchSensibility = preferencesRepository.touchSensibility() - 10,
            longPress = preferencesRepository.customLongPressTimeout().toInt(),
            selected = controlDetails?.controlStyle ?: ControlStyle.Standard,
            controls = gameControlOptions
        )
    }

    override suspend fun mapEventToState(event: ControlEvent) = flow {
        when (event) {
            is ControlEvent.UpdateLongPress -> {
                preferencesRepository.setCustomLongPressTimeout(event.value.toLong())

                val newState = state.copy(
                    longPress = event.value,
                )
                emit(newState)
            }
            is ControlEvent.UpdateTouchSensibility -> {
                preferencesRepository.setTouchSensibility(event.value + 10)
                val newState = state.copy(
                    touchSensibility = event.value,
                )
                emit(newState)
            }
            is ControlEvent.Reset -> {
                preferencesRepository.resetControls()

                val newState = state.copy(
                    longPress = preferencesRepository.customLongPressTimeout().toInt(),
                    touchSensibility = preferencesRepository.touchSensibility() - 10,
                )
                emit(newState)
            }
            is ControlEvent.SelectControlStyle -> {
                val controlStyle = event.controlStyle
                preferencesRepository.useControlStyle(controlStyle)

                val selected = state.controls.first { it.controlStyle == event.controlStyle }

                val newState = state.copy(
                    selected = selected.controlStyle
                )

                emit(newState)
            }
        }
    }
}
