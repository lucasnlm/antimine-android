package dev.lucasnlm.external

import dev.lucasnlm.external.model.CloudSave

class CloudStorageManagerImpl : CloudStorageManager {

    override fun uploadSave(cloudSave: CloudSave) {
        // Todo
    }

    override suspend fun getSave(playId: String): CloudSave? {
        return null
    }
}
