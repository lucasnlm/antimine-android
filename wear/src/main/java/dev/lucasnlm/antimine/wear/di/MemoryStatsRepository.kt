package dev.lucasnlm.antimine.wear.di

import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository

class MemoryStatsRepository : IStatsRepository {
    private val listOfStats = mutableListOf<Stats>()

    override suspend fun getAllStats(): List<Stats> = listOfStats

    override suspend fun addStats(stats: Stats): Long? {
        listOfStats.add(stats)
        return listOfStats.count().toLong()
    }
}
