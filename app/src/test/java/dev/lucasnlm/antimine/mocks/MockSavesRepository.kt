package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.repository.ISavesRepository

class MockSavesRepository : ISavesRepository {
    override suspend fun fetchCurrentSave(): Save? {
        return null
    }

    override suspend fun saveGame(save: Save): Long? {
        return 1
    }

    override fun setLimit(maxSavesStorage: Int) {
        // Empty
    }
}
