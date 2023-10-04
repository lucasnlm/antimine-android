package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.io.models.Save

/**
 * This class is responsible for saving and loading the game.
 */
interface SavesRepository {
    /**
     * @return A list of all save files.
     */
    suspend fun getAllSaves(): List<Save>

    /**
     * @return The current save id. Or null if there is no current save.
     */
    suspend fun currentSaveId(): String?

    /**
     * Get the current save. Or null if there is no current save.
     */
    suspend fun fetchCurrentSave(): Save?

    /**
     * Get the save with the given id. Or null if there is no save with the given id.
     */
    suspend fun loadFromId(id: String): Save?

    /**
     * Save a [Save].
     * Returns the id of the saved file. Or null if the save failed.
     */
    suspend fun saveGame(save: Save): String?
}
