package dev.lucasnlm.antimine.common.io

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SaveListManagerImpl(
    private val context: Context,
    private val saveFileLimit: Int = MAX_SAVE_FILES_COUNT,
) : SaveListManager {
    override suspend fun insertNewSave(filePath: String): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching {
                val saveFileList = readSaveList()

                if (!saveFileList.contains(filePath)) {
                    // Delete old save file
                    if (saveFileList.size >= saveFileLimit) {
                        saveFileList
                            .firstOrNull()
                            ?.let {
                                context.filesDir.resolve(it).delete()
                            }
                    }

                    // Update the list
                    val newSaveList =
                        saveFileList
                            .takeLast(saveFileLimit - 1)
                            .toMutableList()

                    // Update save file list
                    newSaveList.add(filePath)

                    // Save file
                    getSaveListFile()
                        .writeText(newSaveList.joinToString("\n"))
                }
            }.isSuccess
        }
    }

    override suspend fun currentSaveId(): String? {
        return readSaveList().lastOrNull()
    }

    override suspend fun readSaveList(): List<String> {
        return withContext(Dispatchers.IO) {
            val file = getSaveListFile()
            if (!file.exists()) {
                emptyList()
            } else {
                file.readLines()
            }
        }
    }

    private fun getSaveListFile(): File {
        return context.filesDir.resolve(SAVE_FILE_LIST_NAME)
    }

    private companion object {
        const val MAX_SAVE_FILES_COUNT = 5
        const val SAVE_FILE_LIST_NAME = "saves/list"
    }
}
