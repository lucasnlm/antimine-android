package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.level.database.models.Save

interface ISavesRepository {
    suspend fun fetchCurrentSave(): Save?
    suspend fun saveGame(save: Save): Long?
}
