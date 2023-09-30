package dev.lucasnlm.antimine.common.io

import dev.lucasnlm.antimine.common.io.models.StatsFile

/**
 * Handles the file that stores the stats
 */
interface StatsFileManager {
    /**
     * Inserts a new stats
     * @param stats The stats to be inserted
     */
    suspend fun insert(stats: StatsFile)

    /**
     * Deletes the last stats
     */
    suspend fun readStats(): List<StatsFile>

    /**
     * Deletes the last stats
     */
    suspend fun deleteStats()
}
