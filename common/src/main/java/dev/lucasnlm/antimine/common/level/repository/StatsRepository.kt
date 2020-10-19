package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.level.database.dao.StatsDao
import dev.lucasnlm.antimine.common.level.database.models.Stats

interface IStatsRepository {
    suspend fun getAllStats(minId: Int): List<Stats>
    suspend fun addAllStats(stats: List<Stats>): Long?
    suspend fun addStats(stats: Stats): Long?
}

class StatsRepository(
    private val statsDao: StatsDao,
) : IStatsRepository {
    override suspend fun getAllStats(minId: Int): List<Stats> {
        return statsDao.getAll(minId)
    }

    override suspend fun addAllStats(stats: List<Stats>): Long? {
        return statsDao.insertAll(stats).firstOrNull()
    }

    override suspend fun addStats(stats: Stats): Long? {
        return statsDao.insert(stats)
    }
}

class MemoryStatsRepository(
    private val memoryStats: MutableList<Stats> = mutableListOf(),
) : IStatsRepository {
    override suspend fun getAllStats(minId: Int): List<Stats> = memoryStats.filter { it.uid >= minId }

    override suspend fun addAllStats(stats: List<Stats>): Long? {
        memoryStats.addAll(stats)
        return memoryStats.count().toLong()
    }

    override suspend fun addStats(stats: Stats): Long? {
        memoryStats.add(stats)
        return memoryStats.count().toLong()
    }
}
