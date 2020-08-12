package dev.lucasnlm.antimine.control.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.control.models.ControlDetails
import dev.lucasnlm.antimine.core.control.ControlStyle
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import kotlinx.coroutines.flow.flow

class ControlViewModel @ViewModelInject constructor(
    private val preferencesRepository: IPreferencesRepository
) : IntentViewModel<ControlEvent, ControlState>() {

    private val gameControlOptions = listOf(
        ControlDetails(
            id = 0L,
            controlStyle = ControlStyle.Standard,
            titleId = R.string.standard,
            firstActionId = R.string.single_click,
            firstActionResponseId = R.string.open_tile,
            secondActionId = R.string.long_press,
            secondActionResponseId = R.string.flag_tile
        ),
        ControlDetails(
            id = 1L,
            controlStyle = ControlStyle.FastFlag,
            titleId = R.string.flag_first,
            firstActionId = R.string.single_click,
            firstActionResponseId = R.string.flag_tile,
            secondActionId = R.string.long_press,
            secondActionResponseId = R.string.open_tile
        ),
        ControlDetails(
            id = 2L,
            controlStyle = ControlStyle.DoubleClick,
            titleId = R.string.double_click,
            firstActionId = R.string.single_click,
            firstActionResponseId = R.string.flag_tile,
            secondActionId = R.string.double_click,
            secondActionResponseId = R.string.open_tile
        )
    )

    override fun initialState(): ControlState =
        ControlState(
            selectedId = gameControlOptions.firstOrNull {
                it.controlStyle == preferencesRepository.controlStyle()
            }?.id?.toInt() ?: 0,
            gameControls = gameControlOptions
        )

    override suspend fun mapEventToState(event: ControlEvent) = flow {
        if (event is ControlEvent.SelectControlStyle) {
            val controlStyle = event.controlStyle
            preferencesRepository.useControlStyle(controlStyle)

            val newState = state.copy(
                selectedId = state.gameControls.first { it.controlStyle == event.controlStyle }.id.toInt()
            )

            emit(newState)
        }
    }
}
