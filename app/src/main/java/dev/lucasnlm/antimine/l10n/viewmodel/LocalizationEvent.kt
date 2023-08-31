package dev.lucasnlm.antimine.l10n.viewmodel

import java.util.Locale

sealed class LocalizationEvent {
    data object LoadAllLanguages : LocalizationEvent()

    data object FinishActivity : LocalizationEvent()

    data class SetLanguage(val locale: Locale) : LocalizationEvent()
}
