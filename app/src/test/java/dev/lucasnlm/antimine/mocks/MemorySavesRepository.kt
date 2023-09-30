package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.common.io.models.FileSave
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.repository.SavesRepository

class MemorySavesRepository : SavesRepository {
    private var memoryList = mutableListOf<FileSave>()
    private var maxSavesStorage = -1

    override suspend fun getAllSaves(): List<FileSave> = memoryList.toList()

    override suspend fun currentSaveId(): String? {
        return memoryList.firstOrNull()?.id
    }

    override suspend fun fetchCurrentSave(): FileSave? {
        return memoryList.lastOrNull()
    }

    override suspend fun loadFromId(id: String): FileSave? {
        return memoryList.find { it.id == id }
    }

    override suspend fun saveGame(save: FileSave): String {
        if (maxSavesStorage - 1 > 0) {
            memoryList = memoryList.subList(0, maxSavesStorage - 1)
        }
        memoryList.add(save)
        return save.id.orEmpty()
    }
}
