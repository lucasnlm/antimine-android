package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.io.StatsFileManager
import dev.lucasnlm.antimine.common.io.models.Stats

class StatsRepositoryImpl(
    private val statsFileManager: StatsFileManager,
) : StatsRepository {
    override suspend fun getAllStats(): List<Stats> {
        return statsFileManager.readStats()
    }

    override suspend fun addAllStats(stats: List<Stats>) {
        return stats.forEach {
            statsFileManager.insert(it)
        }
    }

    override suspend fun addStats(stats: Stats) {
        statsFileManager.insert(stats)
    }

    override suspend fun deleteLastStats() {
        statsFileManager.deleteStats()
    }
}
