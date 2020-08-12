package dev.lucasnlm.antimine.custom.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.custom.models.CustomState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CreateGameViewModel @ViewModelInject constructor(
    private val preferencesRepository: IPreferencesRepository
) : IntentViewModel<CustomEvent, CustomState>() {
    fun updateCustomGameMode(minefield: Minefield) {
        preferencesRepository.updateCustomGameMode(minefield)
    }

    override fun initialState() =
        preferencesRepository.customGameMode().let {
            CustomState(
                width = it.width,
                height = it.height,
                mines = it.mines,
                valid = true
            )
        }

    override suspend fun mapEventToState(event: CustomEvent) = flow<CustomState> {
        if (event is CustomEvent.ValidateInputEvent) {

        }
    }
}
