package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.io.SaveFileManager
import dev.lucasnlm.antimine.common.io.SaveListManager
import dev.lucasnlm.antimine.common.io.models.Save

class SavesRepositoryImpl(
    private val saveListManager: SaveListManager,
    private val saveFileManager: SaveFileManager,
) : SavesRepository {

    override suspend fun currentSaveId(): String? {
        return saveListManager.currentSaveId()
    }

    override suspend fun getAllSaves(): List<Save> {
        return saveListManager.readSaveList().mapNotNull {
            saveFileManager.loadSave(it)
        }
    }

    override suspend fun fetchCurrentSave(): Save? {
        return saveListManager.currentSaveId()?.let {
            return loadFromId(it)
        }
    }

    override suspend fun loadFromId(id: String): Save? {
        return saveFileManager.loadSave(id)
    }

    override suspend fun saveGame(save: Save): String {
        return saveFileManager.writeSave(save)
    }
}
