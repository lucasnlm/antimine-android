package dev.lucasnlm.antimine.stats.model

import androidx.annotation.StringRes

data class StatsModel(
    @StringRes val title: Int,
    val totalGames: Int,
    val totalTime: Long,
    val victoryTime: Long,
    val averageTime: Long,
    val shortestTime: Long,
    val mines: Int,
    val victory: Int,
    val openArea: Int,
)
