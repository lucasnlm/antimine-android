package dev.lucasnlm.antimine.l10n.viewmodel

import java.util.Locale

sealed class LocalizationEvent {
    object LoadAllLanguages : LocalizationEvent()

    object FinishActivity : LocalizationEvent()

    data class SetLanguage(val locale: Locale) : LocalizationEvent()
}
