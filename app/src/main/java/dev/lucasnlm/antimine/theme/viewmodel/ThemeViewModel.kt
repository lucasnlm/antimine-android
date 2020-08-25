package dev.lucasnlm.antimine.theme.viewmodel

import dev.lucasnlm.antimine.core.analytics.IAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.models.Analytics
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.themes.model.AppTheme
import dev.lucasnlm.antimine.core.themes.repository.IThemeRepository
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.external.IBillingManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class ThemeViewModel(
    private val themeRepository: IThemeRepository,
    private val billingManager: IBillingManager,
    private val preferencesRepository: IPreferencesRepository,
    private val analyticsManager: IAnalyticsManager,
) : IntentViewModel<ThemeEvent, ThemeState>() {
    private fun setTheme(theme: AppTheme) {
        themeRepository.setTheme(theme)
    }

    override fun observeEvent(): Flow<ThemeEvent> {
        return super.observeEvent()
            .onEach {
                if (it is ThemeEvent.ChangeTheme) {
                    analyticsManager.sentEvent(Analytics.ClickTheme(it.newTheme.id))
                }
            }.map {
                if (it is ThemeEvent.ChangeTheme &&
                    billingManager.isEnabled() &&
                    !preferencesRepository.isPremiumEnabled()
                ) {
                    ThemeEvent.Unlock
                } else {
                    it
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
        }
    }

    override fun initialState() = ThemeState(
        current = themeRepository.getTheme(),
        themes = themeRepository.getAllThemes()
    )
}
