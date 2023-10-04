package dev.lucasnlm.antimine.history.viewmodel

import dev.lucasnlm.antimine.common.io.models.Save

data class HistoryState(
    val loading: Boolean,
    val saveList: List<Save>,
)
