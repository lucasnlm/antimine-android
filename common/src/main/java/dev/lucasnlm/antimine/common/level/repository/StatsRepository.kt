package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.io.models.Stats

/**
 * Repository for statistics.
 */
interface StatsRepository {
    /**
     * @return A list of all stats files.
     */
    suspend fun getAllStats(): List<Stats>

    /**
     * Add a list of stats.
     */
    suspend fun addAllStats(stats: List<Stats>)

    /**
     * Add a stats.
     */
    suspend fun addStats(stats: Stats)

    /**
     * Delete the stats file.
     */
    suspend fun deleteLastStats()
}
