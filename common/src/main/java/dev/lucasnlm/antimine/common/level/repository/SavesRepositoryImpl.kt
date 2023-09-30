package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.io.SaveFileManager
import dev.lucasnlm.antimine.common.io.SaveListManager
import dev.lucasnlm.antimine.common.io.models.SaveFile

class SavesRepositoryImpl(
    private val saveListManager: SaveListManager,
    private val saveFileManager: SaveFileManager,
) : SavesRepository {

    override suspend fun currentSaveId(): String? {
        return saveListManager.first()
    }

    override suspend fun getAllSaves(): List<SaveFile> {
        return saveListManager.readSaveList().mapNotNull {
            saveFileManager.loadSave(it)
        }
    }

    override suspend fun fetchCurrentSave(): SaveFile? {
        return saveListManager.first()?.let {
            return loadFromId(it)
        }
    }

    override suspend fun loadFromId(id: String): SaveFile? {
        return saveFileManager.loadSave(id)
    }

    override suspend fun saveGame(save: SaveFile): String {
        return saveFileManager.writeSave(save)
    }
}
