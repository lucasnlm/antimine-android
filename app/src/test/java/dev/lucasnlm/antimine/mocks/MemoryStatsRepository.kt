package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.common.io.models.Stats
import dev.lucasnlm.antimine.common.level.repository.StatsRepository

class MemoryStatsRepository(
    private val memoryStats: MutableList<Stats> = mutableListOf(),
) : StatsRepository {
    override suspend fun getAllStats(): List<Stats> {
        return memoryStats
    }

    override suspend fun addAllStats(stats: List<Stats>) {
        memoryStats.addAll(stats)
    }

    override suspend fun addStats(stats: Stats) {
        memoryStats.add(stats)
    }

    override suspend fun deleteLastStats() {
        memoryStats.clear()
    }
}
