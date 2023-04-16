package dev.lucasnlm.antimine.l10n.viewmodel

import dev.lucasnlm.antimine.l10n.models.GameLanguage

data class LocalizationState(
    val loading: Boolean,
    val languages: List<GameLanguage>,
)
