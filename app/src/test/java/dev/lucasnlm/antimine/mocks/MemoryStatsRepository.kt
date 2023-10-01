package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.common.io.models.StatsFile
import dev.lucasnlm.antimine.common.level.repository.StatsRepository

class MemoryStatsRepository(
    private val memoryStats: MutableList<StatsFile> = mutableListOf(),
) : StatsRepository {
    override suspend fun getAllStats(): List<StatsFile> {
        return memoryStats
    }

    override suspend fun addAllStats(stats: List<StatsFile>) {
        memoryStats.addAll(stats)
    }

    override suspend fun addStats(stats: StatsFile) {
        memoryStats.add(stats)
    }

    override suspend fun deleteLastStats() {
        memoryStats.clear()
    }
}
