package dev.lucasnlm.antimine.themes.viewmodel

import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.external.IAnalyticsManager
import kotlinx.coroutines.flow.flow

class ThemeViewModel(
    private val themeRepository: IThemeRepository,
    private val preferencesRepository: IPreferencesRepository,
    private val analyticsManager: IAnalyticsManager,
) : IntentViewModel<ThemeEvent, ThemeState>() {
    private fun setTheme(theme: AppTheme) {
        themeRepository.setTheme(theme.id)
    }

    override suspend fun mapEventToState(event: ThemeEvent) = flow {
        when (event) {
            is ThemeEvent.SetSquareSize -> {
                preferencesRepository.setSquareSize(event.size + 40)
                emit(
                    state.copy(
                        squareSize = event.size,
                        hasChangedSize = hasChangedSize(),
                    ),
                )
            }
            is ThemeEvent.SetSquareRadius -> {
                preferencesRepository.setSquareRadius(event.radius)
                emit(
                    state.copy(
                        squareRadius = event.radius,
                        hasChangedSize = hasChangedSize(),
                    ),
                )
            }
            is ThemeEvent.SetSquareDivider -> {
                preferencesRepository.setSquareDivider(event.divider)
                emit(
                    state.copy(
                        squareDivider = event.divider,
                        hasChangedSize = hasChangedSize(),
                    ),
                )
            }
            is ThemeEvent.ResetTheme -> {
                preferencesRepository.setSquareRadius(null)
                preferencesRepository.setSquareDivider(null)
                preferencesRepository.setSquareSize(null)

                val newState = state.copy(
                    squareRadius = preferencesRepository.squareRadius(),
                    squareDivider = preferencesRepository.squareDivider(),
                    squareSize = preferencesRepository.squareSize() - 40,
                )

                emit(newState)
            }
            is ThemeEvent.ChangeTheme -> {
                setTheme(event.newTheme)
                analyticsManager.sentEvent(Analytics.ClickTheme(event.newTheme.id))
                emit(state.copy(current = event.newTheme))
                preferencesRepository.addUnlockedTheme(event.newTheme.id.toInt())
            }
            else -> {
                // Ignore
            }
        }
    }

    private fun hasChangedSize(): Boolean {
        return preferencesRepository.defaultSquareSize() != preferencesRepository.squareSize() ||
            preferencesRepository.defaultSquareDivider() != preferencesRepository.squareDivider() ||
            preferencesRepository.defaultSquareRadius() != preferencesRepository.squareRadius()
    }

    override fun initialState() = ThemeState(
        current = themeRepository.getTheme(),
        themes = themeRepository.getAllThemes(),
        squareSize = preferencesRepository.squareSize() - 40,
        squareDivider = preferencesRepository.squareDivider(),
        squareRadius = preferencesRepository.squareRadius(),
        hasChangedSize = hasChangedSize(),
    )
}
