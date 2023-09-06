package dev.lucasnlm.antimine.common.level.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.lucasnlm.antimine.common.level.database.models.Stats

@Dao
interface StatsDao {
    @Query("SELECT * FROM stats WHERE stats.uid >= :minId")
    fun getAll(minId: Int = 0): List<Stats>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stats: Stats): Long

    @Query("DELETE FROM stats WHERE stats.uid in(SELECT MAX(stats.uid) FROM stats)")
    fun deleteLast()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(stats: List<Stats>): LongArray
}
