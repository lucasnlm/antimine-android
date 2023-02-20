package dev.lucasnlm.antimine.preferences

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.VisibleForTesting
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

class SettingsBackupManager(
    private val context: Context,
) {
    @VisibleForTesting
    private fun filterDataToExport(data: Map<String, Any?>): Map<String, Any?> {
        return data.filter {
            it.key != PreferenceKeys.PREFERENCE_PREMIUM_FEATURES
        }.map {
            it.key.replace(PREFERENCE_SUFFIX, "") to it.value
        }.toMap()
    }

    private fun filterDataToImport(data: Map<String, Any?>?): Map<String, Any?>? {
        return data?.filter {
            it.key != PreferenceKeys.PREFERENCE_PREMIUM_FEATURES || it.key.contains("premium")
        }?.map {
            val key = it.key
            "${PREFERENCE_SUFFIX}$key" to it.value
        }?.toMap()
    }

    fun importSettings(location: Uri): Map<String, Any?>? {
        val contentResolver: ContentResolver = context.contentResolver
        var result: Map<String, Any?>? = null

        try {
            contentResolver.openInputStream(location)?.use { stream ->
                val bytes = stream.readAllBytesCompat()
                val jsonStr = String(bytes, StandardCharsets.UTF_8)
                result = jsonStringToMap(jsonStr)

                if (result?.get(PACKAGE_KEY) != PACKAGE) {
                    return null
                }

                result = filterDataToImport(result)
            }
        } catch (e: Exception) {
            // Fail to write, probably.
        }

        return result
    }

    fun exportSettings(location: Uri, exportData: Map<String, Any?>): Boolean {
        var result = true

        val contentResolver: ContentResolver = context.contentResolver
        val filteredData = filterDataToExport(exportData).toMutableMap()
        filteredData[PACKAGE_KEY] = PACKAGE
        val jsonData = mapToJsonString(filteredData)

        try {
            contentResolver.openOutputStream(location)?.use { stream ->
                stream.write(jsonData.toByteArray())
            }
        } catch (e: Exception) {
            // Fail to write, probably.
            result = false
        }

        return result
    }

    @Throws(IOException::class)
    private fun InputStream.readAllBytesCompat(): ByteArray {
        val bufLen = 4 * 0x400 // 4KB
        val buf = ByteArray(bufLen)
        var readLen: Int

        ByteArrayOutputStream().use { steam ->
            this.use { i ->
                while (i.read(buf, 0, bufLen).also { readLen = it } != -1) {
                    steam.write(buf, 0, readLen)
                }
            }

            return steam.toByteArray()
        }
    }

    private fun mapToJsonString(exportData: Map<String, Any?>): String {
        return JSONObject(exportData).toString(2)
    }

    private fun jsonStringToMap(importData: String): Map<String, Any?>? {
        return try {
            JSONObject(importData).toMap()
        } catch (ignored: Exception) {
            // Something went wrong. Can't import invalid json.
            null
        }
    }

    private fun JSONObject.toMap(): Map<String, Any?> {
        return keys().asSequence().associateWith { opt(it) }
    }

    companion object {
        const val FILE_NAME = "antimine-settings-backup.json"
        const val PREFERENCE_SUFFIX = "preference_"
        const val PACKAGE_KEY = "package"
        const val PACKAGE = "dev.lucasnlm.antimine"
    }
}
