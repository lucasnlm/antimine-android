package dev.lucasnlm.antimine.common.level.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.lucasnlm.antimine.common.level.database.models.Stats

@Dao
interface StatsDao {
    @Query("SELECT * FROM stats")
    suspend fun getAll(): List<Stats>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg stats: Stats): LongArray
}
