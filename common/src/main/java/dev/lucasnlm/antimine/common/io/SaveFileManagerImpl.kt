package dev.lucasnlm.antimine.common.io

import android.content.Context
import dev.lucasnlm.antimine.common.io.models.FileSave
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class SaveFileManagerImpl(
    private val context: Context,
    private val saveListManager: SaveListManagerImpl,
    private val scope: CoroutineScope,
) : SaveFileManager {
    override suspend fun loadSave(filePath: String): FileSave? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val content = context.filesDir.resolve(filePath).readText()
                FileSave.fromString(content)
            }.getOrNull()
        }
    }

    override suspend fun writeSave(save: FileSave): String {
        val filePath = save.id ?: createRandomFileName()
        scope.launch {
            var result = writeSaveFile(filePath, save)
            if (result) {
                result = saveListManager.insertNewSave(filePath)
            }
            if (!result) {
                deleteSaveFile(filePath)
            }
        }
        return filePath
    }

    private suspend fun deleteSaveFile(filePath: String) {
        withContext(Dispatchers.IO) {
            context.filesDir.resolve(filePath).delete()
        }
    }

    private suspend fun writeSaveFile(filePath: String, save: FileSave): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching {
                val file = File(context.filesDir, filePath).apply {
                    mkdirs()
                    delete()
                }

                file.outputStream().use {
                    it.write(save.serialize().toByteArray())
                }

                true
            }.isSuccess
        }
    }

    private fun saveFilePath(id: String): String {
        return "saves/$id.save"
    }

    private fun createRandomFileName(): String {
        return saveFilePath(UUID.randomUUID().toString())
    }
}
