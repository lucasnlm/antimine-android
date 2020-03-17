package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.level.database.dao.SaveDao
import dev.lucasnlm.antimine.common.level.database.models.Save
import javax.inject.Inject

interface ISavesRepository {
    suspend fun getNewSaveId(): Int
    suspend fun fetchCurrentSave(): Save?
    suspend fun saveGame(save: Save): Long?
}

class SavesRepository @Inject constructor(
    private val savesDao: SaveDao
) : ISavesRepository {

    override suspend fun getNewSaveId(): Int = savesDao.getSaveCounts() + 1

    override suspend fun fetchCurrentSave(): Save? = savesDao.loadCurrent()

    override suspend fun saveGame(save: Save) = savesDao.insertAll(save).firstOrNull()
}
