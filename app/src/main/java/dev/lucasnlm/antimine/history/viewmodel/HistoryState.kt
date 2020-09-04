package dev.lucasnlm.antimine.history.viewmodel

import dev.lucasnlm.antimine.common.level.database.models.Save

data class HistoryState(
    val saveList: List<Save>,
    val showAds: Boolean,
)
