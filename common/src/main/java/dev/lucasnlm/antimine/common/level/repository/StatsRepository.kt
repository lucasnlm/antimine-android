package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.level.database.models.Stats

interface StatsRepository {
    suspend fun getAllStats(minId: Int): List<Stats>

    suspend fun addAllStats(stats: List<Stats>): Long?

    suspend fun addStats(stats: Stats): Long?

    suspend fun deleteLastStats()
}
