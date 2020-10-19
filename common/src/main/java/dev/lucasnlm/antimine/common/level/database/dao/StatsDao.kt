package dev.lucasnlm.antimine.common.level.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.lucasnlm.antimine.common.level.database.models.Stats

@Dao
interface StatsDao {
    @Query("SELECT * FROM stats WHERE stats.uid >= :minId")
    suspend fun getAll(minId: Int = 0): List<Stats>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: Stats): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stats: List<Stats>): LongArray
}
