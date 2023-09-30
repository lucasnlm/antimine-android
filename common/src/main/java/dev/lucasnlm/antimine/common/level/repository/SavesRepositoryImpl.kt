package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.io.SaveFileManager
import dev.lucasnlm.antimine.common.io.SaveListManager
import dev.lucasnlm.antimine.common.io.models.FileSave

class SavesRepositoryImpl(
    private val saveListManager: SaveListManager,
    private val saveFileManager: SaveFileManager,
) : SavesRepository {

    override suspend fun currentSaveId(): String? {
        return saveListManager.first()
    }

    override suspend fun getAllSaves(): List<FileSave> {
        return saveListManager.readSaveList().mapNotNull {
            saveFileManager.loadSave(it)
        }
    }

    override suspend fun fetchCurrentSave(): FileSave? {
        return saveListManager.first()?.let {
            return loadFromId(it)
        }
    }

    override suspend fun loadFromId(id: String): FileSave? {
        return saveFileManager.loadSave(id)
    }

    override suspend fun saveGame(save: FileSave): String {
        return saveFileManager.writeSave(save)
    }
}
