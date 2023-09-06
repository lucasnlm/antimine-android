package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.level.database.models.Save

interface SavesRepository {
    suspend fun getAllSaves(): List<Save>

    suspend fun fetchCurrentSave(): Save?

    suspend fun loadFromId(id: Int): Save?

    suspend fun saveGame(save: Save): Long?

    fun setLimit(maxSavesStorage: Int)
}
