package dev.lucasnlm.antimine.stats.model

data class StatsModel(
    val totalGames: Int,
    val duration: Long,
    val averageDuration: Long,
    val mines: Int,
    val victory: Int,
    val openArea: Int,
    val showAds: Boolean,
)
