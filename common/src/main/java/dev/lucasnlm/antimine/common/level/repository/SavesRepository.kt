package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.level.database.dao.SaveDao
import dev.lucasnlm.antimine.common.level.database.models.Save

interface ISavesRepository {
    suspend fun getAllSaves(): List<Save>
    suspend fun fetchCurrentSave(): Save?
    suspend fun loadFromId(id: Int): Save?
    suspend fun saveGame(save: Save): Long?
    fun setLimit(maxSavesStorage: Int)
}

class SavesRepository(
    private val savesDao: SaveDao,
    private var maxSavesStorage: Int = MAX_STORAGE,
) : ISavesRepository {
    override suspend fun getAllSaves(): List<Save> = savesDao.getAll()

    override suspend fun fetchCurrentSave(): Save? = savesDao.loadCurrent()

    override suspend fun loadFromId(id: Int): Save? = savesDao.loadFromId(id)

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

class MemorySavesRepository : ISavesRepository {
    private var memoryList = mutableListOf<Save>()
    private var maxSavesStorage = -1

    override suspend fun getAllSaves(): List<Save> = memoryList.toList()

    override suspend fun fetchCurrentSave(): Save? = memoryList.lastOrNull()

    override suspend fun loadFromId(id: Int): Save? = memoryList.find { it.uid == id }

    override suspend fun saveGame(save: Save): Long? {
        if (maxSavesStorage - 1 > 0) {
            memoryList = memoryList.subList(0, maxSavesStorage - 1)
        }
        memoryList.add(save)
        return memoryList.count().toLong()
    }

    override fun setLimit(maxSavesStorage: Int) {
        this.maxSavesStorage = maxSavesStorage
        memoryList = memoryList.subList(0, maxSavesStorage)
    }
}
