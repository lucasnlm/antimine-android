package dev.lucasnlm.antimine.common.io

import dev.lucasnlm.antimine.common.io.models.Save

interface SaveFileManager {
    /**
     * Loads a save file from a given path.
     * @param filePath The path of the save file.
     * @return The save file or null if it could not be loaded.
     */
    suspend fun loadSave(filePath: String): Save?

    /**
     * Writes a save file.
     * @param save The save file to be written.
     */
    suspend fun writeSave(save: Save): String
}
