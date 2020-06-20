package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.level.database.dao.StatsDao
import dev.lucasnlm.antimine.common.level.database.models.Stats

interface IStatsRepository {
    suspend fun getAllStats(): List<Stats>
    suspend fun addStats(stats: Stats): Long?
}

class StatsRepository(
    private val statsDao: StatsDao
) : IStatsRepository {
    override suspend fun getAllStats(): List<Stats> {
        return statsDao.getAll()
    }

    override suspend fun addStats(stats: Stats): Long? {
        return statsDao.insertAll(stats).firstOrNull()
    }
}

class MemoryStatsRepository(
    private val memoryStats: MutableList<Stats> = mutableListOf()
) : IStatsRepository {
    override suspend fun getAllStats(): List<Stats> = memoryStats.toList()

    override suspend fun addStats(stats: Stats): Long? {
        memoryStats.add(stats)
        return memoryStats.count().toLong()
    }
}
