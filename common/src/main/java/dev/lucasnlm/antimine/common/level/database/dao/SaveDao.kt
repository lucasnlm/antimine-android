package dev.lucasnlm.antimine.common.level.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.lucasnlm.antimine.common.level.database.models.Save

@Dao
interface SaveDao {
    @Query("SELECT * FROM save")
    fun getAll(): List<Save>

    @Query("SELECT * FROM save WHERE uid IN (:gameIds)")
    fun loadAllByIds(gameIds: IntArray): List<Save>

    @Query("SELECT * FROM save WHERE uid = :gameId LIMIT 1")
    fun loadById(gameId: Int): Save

    @Query("SELECT * FROM save ORDER BY uid DESC LIMIT 1")
    fun loadCurrent(): Save?

    @Query("SELECT count(uid) FROM save")
    fun getSaveCounts(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg saves: Save): Array<Long>

    @Delete
    fun delete(save: Save)

    @Query("DELETE FROM save WHERE uid NOT IN (SELECT uid FROM save LIMIT :maxStorage)")
    fun deleteOldSaves(maxStorage: Int)
}
