package dev.lucasnlm.antimine.control.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.control.model.ControlModel
import dev.lucasnlm.antimine.core.control.ControlStyle
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

class ControlViewModel @ViewModelInject constructor(
    private val preferencesRepository: IPreferencesRepository
) : ViewModel() {
    val controlTypeSelected = MutableLiveData<ControlStyle>(preferencesRepository.controlType())

    val gameControlOptions = listOf(
        ControlModel(
            id = 0L,
            controlStyle = ControlStyle.Standard,
            titleId = R.string.standard,
            firstActionId = R.string.single_click,
            firstActionResponseId = R.string.open_tile,
            secondActionId = R.string.long_press,
            secondActionResponseId = R.string.flag_tile
        ),
        ControlModel(
            id = 1L,
            controlStyle = ControlStyle.FastFlag,
            titleId = R.string.flag_first,
            firstActionId = R.string.single_click,
            firstActionResponseId = R.string.flag_tile,
            secondActionId = R.string.long_press,
            secondActionResponseId = R.string.open_tile
        ),
        ControlModel(
            id = 2L,
            controlStyle = ControlStyle.DoubleClick,
            titleId = R.string.double_click,
            firstActionId = R.string.single_click,
            firstActionResponseId = R.string.flag_tile,
            secondActionId = R.string.double_click,
            secondActionResponseId = R.string.open_tile
        )
    )

    init {
        controlTypeSelected.postValue(preferencesRepository.controlType())
    }

    fun selectControlType(controlStyle: ControlStyle) {
        preferencesRepository.useControlType(controlStyle)
        controlTypeSelected.postValue(controlStyle)
    }
}