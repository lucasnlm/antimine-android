package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.repository.SavesRepository

class MemorySavesRepository : SavesRepository {
    private var memoryList = mutableListOf<Save>()
    private var maxSavesStorage = -1

    override suspend fun getAllSaves(): List<Save> = memoryList.toList()

    override suspend fun fetchCurrentSave(): Save? = memoryList.lastOrNull()

    override suspend fun loadFromId(id: Int): Save? = memoryList.find { it.uid == id }

    override suspend fun saveGame(save: Save): Long {
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
