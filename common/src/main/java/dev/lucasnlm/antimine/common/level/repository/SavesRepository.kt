package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.io.models.SaveFile

interface SavesRepository {
    suspend fun getAllSaves(): List<SaveFile>

    suspend fun currentSaveId(): String?

    suspend fun fetchCurrentSave(): SaveFile?

    suspend fun loadFromId(id: String): SaveFile?

    suspend fun saveGame(save: SaveFile): String?
}
