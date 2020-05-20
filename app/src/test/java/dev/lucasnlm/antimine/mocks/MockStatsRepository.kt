package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository

class MockStatsRepository(
    private val list: List<Stats>
) : IStatsRepository {
    override suspend fun getAllStats(): List<Stats> = list

    override suspend fun addStats(stats: Stats): Long? {
        return null
    }
}
