package dev.lucasnlm.antimine.common.io

import dev.lucasnlm.antimine.common.level.database.models.Stats

/**
 * Handles the file that stores the stats
 */
interface StatsFileManager {
    /**
     * Inserts a new stats
     * @param stats The stats to be inserted
     */
    fun insert(stats: Stats)

    /**
     * Deletes the last stats
     */
    fun readStats(): List<Stats>
}
