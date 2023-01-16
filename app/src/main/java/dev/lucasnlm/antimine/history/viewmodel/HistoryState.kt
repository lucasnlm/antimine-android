package dev.lucasnlm.antimine.history.viewmodel

import dev.lucasnlm.antimine.common.level.database.models.Save

data class HistoryState(
    val loading: Boolean,
    val saveList: List<Save>,
)
