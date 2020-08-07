package dev.lucasnlm.antimine.custom.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

class CreateGameViewModel @ViewModelInject constructor(
    private val preferencesRepository: IPreferencesRepository
) : ViewModel() {
    fun updateCustomGameMode(minefield: Minefield) {
        preferencesRepository.updateCustomGameMode(minefield)
    }
}
