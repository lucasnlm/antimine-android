package dev.lucasnlm.antimine.custom.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.viewmodel.StatelessViewModel

class CreateGameViewModel @ViewModelInject constructor(
    private val preferencesRepository: IPreferencesRepository
) : StatelessViewModel<CustomEvent>() {
    override fun onEvent(event: CustomEvent) {
        when (event) {
            is CustomEvent.UpdateCustomGameEvent -> {
                preferencesRepository.updateCustomGameMode(event.minefield)
            }
        }
    }
}
