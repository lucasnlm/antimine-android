package dev.lucasnlm.antimine.l10n.viewmodel

import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.l10n.GameLocaleManager
import dev.lucasnlm.antimine.l10n.models.GameLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.*

class LocalizationViewModel(
    private val audioManager: GameAudioManager,
    private val gameLocaleManager: GameLocaleManager,
) : IntentViewModel<LocalizationEvent, LocalizationState>() {
    override fun initialState(): LocalizationState {
        return LocalizationState(
            loading = true,
            languages = listOf(),
        )
    }

    override suspend fun mapEventToState(event: LocalizationEvent) =
        flow {
            when (event) {
                is LocalizationEvent.LoadAllLanguages -> {
                    emit(state.copy(loading = true))
                    val languages = loadLocaleList()
                    emit(state.copy(loading = false, languages = languages))
                }
                is LocalizationEvent.SetLanguage -> {
                    val locale = event.locale.toLanguageTag()
                    withContext(Dispatchers.Main) {
                        audioManager.playClickSound()
                        gameLocaleManager.setGameLocale(locale)
                        gameLocaleManager.applyPreferredLocaleIfNeeded()
                        sendSideEffect(LocalizationEvent.FinishActivity)
                    }
                }
                else -> {
                    // Ignore
                }
            }
        }

    private fun appLocales(): List<Locale> {
        return gameLocaleManager.getAllGameLocaleTags().map {
            Locale.Builder().setLanguageTag(it).build()
        }
    }

    private fun Locale.getNativeName(): String {
        return getDisplayName(this).replaceFirstChar {
            if (it.isLowerCase()) {
                it.titlecase(this)
            } else {
                it.toString()
            }
        }
    }

    private fun loadLocaleList(): List<GameLanguage> {
        return appLocales().mapIndexed { index, locale ->
            GameLanguage(
                id = index,
                locale = locale,
                name = locale.getNativeName(),
            )
        }.sortedBy {
            it.name
        }
    }
}
