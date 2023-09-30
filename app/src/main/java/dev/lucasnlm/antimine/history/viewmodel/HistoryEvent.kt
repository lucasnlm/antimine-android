package dev.lucasnlm.antimine.history.viewmodel

sealed class HistoryEvent {
    data object LoadAllSaves : HistoryEvent()

    data class ReplaySave(val id: String) : HistoryEvent()

    data class LoadSave(val id: String) : HistoryEvent()
}
