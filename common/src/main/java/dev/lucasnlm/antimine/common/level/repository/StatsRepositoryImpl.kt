package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.io.StatsFileManager
import dev.lucasnlm.antimine.common.io.models.StatsFile

class StatsRepositoryImpl(
    private val statsFileManager: StatsFileManager,
) : StatsRepository {
    override suspend fun getAllStats(): List<StatsFile> {
        return statsFileManager.readStats()
    }

    override suspend fun addAllStats(stats: List<StatsFile>) {
        return stats.forEach {
            statsFileManager.insert(it)
        }
    }

    override suspend fun addStats(stats: StatsFile) {
        statsFileManager.insert(stats)
    }

    override suspend fun deleteLastStats() {
        statsFileManager.deleteStats()
    }
}
