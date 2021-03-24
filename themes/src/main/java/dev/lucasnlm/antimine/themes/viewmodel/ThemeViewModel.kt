package dev.lucasnlm.antimine.themes.viewmodel

import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.external.IAnalyticsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class ThemeViewModel(
    private val themeRepository: IThemeRepository,
    private val preferencesRepository: IPreferencesRepository,
    private val analyticsManager: IAnalyticsManager,
) : IntentViewModel<ThemeEvent, ThemeState>() {
    private fun setTheme(theme: AppTheme) {
        themeRepository.setTheme(theme.id)
    }

    override fun observeEvent(): Flow<ThemeEvent> {
        return super.observeEvent()
            .onEach {
                if (it is ThemeEvent.ChangeTheme) {
                    analyticsManager.sentEvent(Analytics.ClickTheme(it.newTheme.id))
                }
            }
    }

    override suspend fun mapEventToState(event: ThemeEvent) = flow {
        if (event is ThemeEvent.ResetTheme) {
            val defaultTheme = themeRepository.reset()
            emit(state.copy(current = defaultTheme))
        } else if (event is ThemeEvent.ChangeTheme) {
            setTheme(event.newTheme)
            emit(state.copy(current = event.newTheme))
            preferencesRepository.addUnlockedTheme(event.newTheme.id.toInt())
        }
    }

    override fun initialState() = ThemeState(
        current = themeRepository.getTheme(),
        themes = themeRepository.getAllThemes()
    )
}
