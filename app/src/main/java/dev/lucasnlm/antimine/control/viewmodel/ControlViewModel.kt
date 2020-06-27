package dev.lucasnlm.antimine.control.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.core.control.ControlStyle
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

class ControlViewModel @ViewModelInject constructor(
    private val preferencesRepository: IPreferencesRepository
) : ViewModel() {
    val controlTypeSelected = MutableLiveData<ControlStyle>(preferencesRepository.controlType())

    val gameControlOptions = listOf(
        ControlStyle.Standard, ControlStyle.FastFlag, ControlStyle.DoubleClick
    )

    init {
        controlTypeSelected.postValue(preferencesRepository.controlType())
    }

    fun selectControlType(controlStyle: ControlStyle) {
        preferencesRepository.useControlType(controlStyle)
        controlTypeSelected.postValue(controlStyle)
    }
}
