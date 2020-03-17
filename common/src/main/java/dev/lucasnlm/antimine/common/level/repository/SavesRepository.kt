package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.level.database.dao.SaveDao
import dev.lucasnlm.antimine.common.level.database.models.Save
import javax.inject.Inject

class SavesRepository @Inject constructor(
    private val savesDao: SaveDao
) : ISavesRepository {

    override suspend fun fetchCurrentSave(): Save? = savesDao.loadCurrent()

    override suspend fun saveGame(save: Save) = savesDao.insertAll(save).firstOrNull()
}
