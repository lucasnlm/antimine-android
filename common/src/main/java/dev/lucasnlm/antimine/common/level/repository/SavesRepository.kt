package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.io.models.FileSave

interface SavesRepository {
    suspend fun getAllSaves(): List<FileSave>

    suspend fun currentSaveId(): String?

    suspend fun fetchCurrentSave(): FileSave?

    suspend fun loadFromId(id: String): FileSave?

    suspend fun saveGame(save: FileSave): String?
}
