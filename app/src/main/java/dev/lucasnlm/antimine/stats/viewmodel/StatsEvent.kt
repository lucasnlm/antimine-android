package dev.lucasnlm.antimine.stats.viewmodel

sealed class StatsEvent {
    object LoadStats : StatsEvent()
    object DeleteStats : StatsEvent()
}
