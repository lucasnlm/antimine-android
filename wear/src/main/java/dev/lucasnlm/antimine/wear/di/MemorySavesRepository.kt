package dev.lucasnlm.antimine.wear.di

import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository

class MemorySavesRepository : ISavesRepository {
    private var currentSave: Save? = null

    override suspend fun getAllSaves(): List<Save> = listOf()

    override suspend fun fetchCurrentSave(): Save? = currentSave

    override suspend fun loadFromId(id: Int): Save? = currentSave

    override suspend fun saveGame(save: Save): Long? {
        currentSave = save
        return 1L
    }

    override fun setLimit(maxSavesStorage: Int) { }
}
