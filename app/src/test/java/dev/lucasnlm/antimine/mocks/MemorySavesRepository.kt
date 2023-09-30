package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.common.io.models.SaveFile
import dev.lucasnlm.antimine.common.level.repository.SavesRepository

class MemorySavesRepository : SavesRepository {
    private var memoryList = mutableListOf<SaveFile>()
    private var maxSavesStorage = -1

    override suspend fun getAllSaves(): List<SaveFile> = memoryList.toList()

    override suspend fun currentSaveId(): String? {
        return memoryList.firstOrNull()?.id
    }

    override suspend fun fetchCurrentSave(): SaveFile? {
        return memoryList.lastOrNull()
    }

    override suspend fun loadFromId(id: String): SaveFile? {
        return memoryList.find { it.id == id }
    }

    override suspend fun saveGame(save: SaveFile): String {
        if (maxSavesStorage - 1 > 0) {
            memoryList = memoryList.subList(0, maxSavesStorage - 1)
        }
        memoryList.add(save)
        return save.id.orEmpty()
    }
}
