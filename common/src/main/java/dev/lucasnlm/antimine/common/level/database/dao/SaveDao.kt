package dev.lucasnlm.antimine.common.level.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.lucasnlm.antimine.common.level.database.models.Save

@Dao
interface SaveDao {
    @Query("SELECT * FROM new_save")
    suspend fun getAll(): List<Save>

    @Query("SELECT * FROM new_save WHERE uid = :gameId LIMIT 1")
    suspend fun loadFromId(gameId: Int): Save

    @Query("SELECT * FROM new_save ORDER BY uid DESC LIMIT 1")
    suspend fun loadCurrent(): Save?

    @Query("SELECT count(uid) FROM new_save")
    suspend fun getSaveCounts(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg saves: Save): LongArray

    @Delete
    suspend fun delete(save: Save)

    @Query("DELETE FROM new_save WHERE uid NOT IN (SELECT uid FROM new_save WHERE status != 1 LIMIT :maxStorage)")
    suspend fun deleteOldSaves(maxStorage: Int)
}
