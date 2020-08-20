package dev.lucasnlm.antimine.theme.viewmodel

import dev.lucasnlm.antimine.core.themes.model.AppTheme
import dev.lucasnlm.antimine.core.themes.repository.IThemeRepository
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import kotlinx.coroutines.flow.flow

class ThemeViewModel(
    private val themeRepository: IThemeRepository
) : IntentViewModel<ThemeEvent, ThemeState>() {
    private fun setTheme(theme: AppTheme) {
        themeRepository.setTheme(theme)
    }

    override suspend fun mapEventToState(event: ThemeEvent) = flow {
        if (event is ThemeEvent.ChangeTheme) {
            setTheme(event.newTheme)
            emit(state.copy(current = event.newTheme))
        }
    }

    override fun initialState() = ThemeState(
        current = themeRepository.getTheme(),
        themes = themeRepository.getAllThemes()
    )
}
