package dev.lucasnlm.antimine.common.io

/**
 * Save list manager.
 */
interface SaveListManager {
    /**
     * Inserts a new save file into the list.
     * @param filePath The path of the save file.
     * @return True if the save file was inserted, false otherwise.
     */
    suspend fun insertNewSave(filePath: String): Boolean

    /**
     * Returns the first save file in the list.
     * Or null if there is no save file.
     */
    suspend fun currentSaveId(): String?

    /**
     * Returns the list of save files.
     */
    suspend fun readSaveList(): List<String>
}
