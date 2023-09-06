package dev.lucasnlm.antimine.themes.viewmodel

import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.ui.model.AppSkin
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.repository.ThemeRepository
import dev.lucasnlm.external.AnalyticsManager
import kotlinx.coroutines.flow.flow

class ThemeViewModel(
    private val themeRepository: ThemeRepository,
    private val analyticsManager: AnalyticsManager,
) : IntentViewModel<ThemeEvent, ThemeState>() {
    private fun setTheme(theme: AppTheme) {
        themeRepository.setTheme(theme.id)
    }

    private fun setSkin(skin: AppSkin) {
        themeRepository.setSkin(skin.id)
    }

    override suspend fun mapEventToState(event: ThemeEvent) =
        flow {
            when (event) {
                is ThemeEvent.ChangeTheme -> {
                    setTheme(event.newTheme)
                    analyticsManager.sentEvent(Analytics.ClickTheme(event.newTheme.id))
                    emit(state.copy(currentTheme = event.newTheme))
                }
                is ThemeEvent.ChangeSkin -> {
                    setSkin(event.newSkin)
                    analyticsManager.sentEvent(Analytics.ClickSkin(event.newSkin.id))
                    emit(state.copy(currentAppSkin = event.newSkin))
                }
                else -> {
                    // Ignore
                }
            }
        }

    override fun initialState() =
        ThemeState(
            currentTheme = themeRepository.getTheme(),
            currentAppSkin = themeRepository.getSkin(),
            themes = themeRepository.getAllThemes(),
            appSkins = themeRepository.getAllSkins(),
        )
}
