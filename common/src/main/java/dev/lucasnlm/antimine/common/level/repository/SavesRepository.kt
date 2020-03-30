package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.level.database.dao.SaveDao
import dev.lucasnlm.antimine.common.level.database.models.Save
import javax.inject.Inject

interface ISavesRepository {
    suspend fun getAllSaves(): List<Save>
    suspend fun fetchCurrentSave(): Save?
    suspend fun saveGame(save: Save): Long?
    fun setLimit(maxSavesStorage: Int)
}

class SavesRepository @Inject constructor(
    private val savesDao: SaveDao,
    private var maxSavesStorage: Int = MAX_STORAGE
) : ISavesRepository {
    override suspend fun getAllSaves(): List<Save> = savesDao.getAll()

    override suspend fun fetchCurrentSave(): Save? = savesDao.loadCurrent()

    override suspend fun saveGame(save: Save): Long? = with(savesDao) {
        if (getSaveCounts() >= maxSavesStorage) {
            deleteOldSaves(maxSavesStorage)
        }
        return insertAll(save).firstOrNull()
    }

    override fun setLimit(maxSavesStorage: Int) {
        this.maxSavesStorage = maxSavesStorage
    }

    companion object {
        private const val MAX_STORAGE = 15
    }
}
