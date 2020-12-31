package dev.lucasnlm.antimine.custom.viewmodel

import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import kotlinx.coroutines.flow.flow

class CreateGameViewModel(
    private val preferencesRepository: IPreferencesRepository,
) : IntentViewModel<CustomEvent, CustomState>() {

    override suspend fun mapEventToState(event: CustomEvent) = flow {
        if (event is CustomEvent.UpdateCustomGameEvent) {
            val minefield = event.minefield
            preferencesRepository.updateCustomGameMode(minefield)
            emit(CustomState(minefield.width, minefield.height, minefield.mines))
        }
    }

    override fun initialState() = with(preferencesRepository.customGameMode()) {
        CustomState(width, height, mines)
    }
}
