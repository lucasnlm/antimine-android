package dev.lucasnlm.antimine.common.io

import android.content.Context
import dev.lucasnlm.antimine.common.io.models.FileSave
import dev.lucasnlm.antimine.common.io.serializer.FileSaveSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class SaveFileManagerImpl(
    private val context: Context,
    private val saveListManager: SaveListManager,
    private val scope: CoroutineScope,
) : SaveFileManager {
    override suspend fun loadSave(filePath: String): FileSave? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val content = context.filesDir.resolve(filePath).readBytes()
                FileSaveSerializer.deserialize(filePath, content)
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
                File(context.filesDir, filePath).apply {
                    mkdirs()
                    delete()

                    writeBytes(
                        FileSaveSerializer.serialize(save),
                    )
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
