package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.io.models.StatsFile

interface StatsRepository {
    suspend fun getAllStats(): List<StatsFile>

    suspend fun addAllStats(stats: List<StatsFile>)

    suspend fun addStats(stats: StatsFile)

    suspend fun deleteLastStats()
}
