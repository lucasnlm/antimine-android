package dev.lucasnlm.antimine.stats.model

import androidx.annotation.StringRes

data class StatsModel(
    @StringRes val title: Int,
    val totalGames: Int,
    val duration: Long,
    val averageDuration: Long,
    val mines: Int,
    val victory: Int,
    val openArea: Int,
)
