package dev.lucasnlm.antimine.stats.viewmodel

sealed class StatsEvent {
    data object LoadStats : StatsEvent()

    data object DeleteStats : StatsEvent()
}
