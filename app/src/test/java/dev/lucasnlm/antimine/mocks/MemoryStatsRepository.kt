package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.common.level.repository.StatsRepository

class MemoryStatsRepository(
    private val memoryStats: MutableList<Stats> = mutableListOf(),
) : StatsRepository {
    override suspend fun getAllStats(minId: Int): List<Stats> = memoryStats.filter { it.uid >= minId }

    override suspend fun addAllStats(stats: List<Stats>): Long {
        memoryStats.addAll(stats)
        return memoryStats.count().toLong()
    }

    override suspend fun addStats(stats: Stats): Long {
        memoryStats.add(stats)
        return memoryStats.count().toLong()
    }

    override suspend fun deleteLastStats() {
        if (memoryStats.isNotEmpty()) {
            memoryStats.removeLast()
        }
    }
}
