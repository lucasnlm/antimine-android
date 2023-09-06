package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.level.database.dao.SaveDao
import dev.lucasnlm.antimine.common.level.database.models.Save
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SavesRepositoryImpl(
    private val savesDao: SaveDao,
    private var maxSavesStorage: Int = MAX_STORAGE,
) : SavesRepository {
    override suspend fun getAllSaves(): List<Save> =
        withContext(Dispatchers.IO) {
            savesDao.getAll()
        }

    override suspend fun fetchCurrentSave(): Save? =
        withContext(Dispatchers.IO) {
            savesDao.loadCurrent()
        }

    override suspend fun loadFromId(id: Int): Save =
        withContext(Dispatchers.IO) {
            savesDao.loadFromId(id)
        }

    override suspend fun saveGame(save: Save): Long? =
        with(savesDao) {
            return withContext(Dispatchers.IO) {
                if (getSaveCounts() >= maxSavesStorage) {
                    deleteOldSaves(maxSavesStorage)
                }
                insertAll(save).firstOrNull()
            }
        }

    override fun setLimit(maxSavesStorage: Int) {
        this.maxSavesStorage = maxSavesStorage
    }

    companion object {
        private const val MAX_STORAGE = 15
    }
}
