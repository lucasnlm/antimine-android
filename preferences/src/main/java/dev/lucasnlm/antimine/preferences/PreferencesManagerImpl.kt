package dev.lucasnlm.antimine.preferences

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class PreferencesManagerImpl(
    private val context: Context,
) : PreferencesManager {
    private val preferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ) = preferences.getBoolean(key, defaultValue)

    override fun putBoolean(
        key: String,
        value: Boolean,
    ) = preferences.edit { putBoolean(key, value) }

    override fun getInt(
        key: String,
        defaultValue: Int,
    ) = preferences.getInt(key, defaultValue)

    override fun getIntOrNull(key: String): Int? {
        return if (preferences.contains(key)) {
            preferences.getInt(key, -1)
        } else {
            null
        }
    }

    override fun getLongOrNull(key: String): Long? {
        return if (preferences.contains(key)) {
            preferences.getLong(key, -1)
        } else {
            null
        }
    }

    override fun putInt(
        key: String,
        value: Int,
    ) = preferences.edit { putInt(key, value) }

    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun putString(
        key: String,
        value: String,
    ) = preferences.edit { putString(key, value) }

    override fun contains(key: String): Boolean = preferences.contains(key)

    override fun removeKey(key: String) = preferences.edit { remove(key) }

    override fun clear() = preferences.edit { clear() }

    override fun getLong(
        key: String,
        defaultValue: Long,
    ): Long = preferences.getLong(key, defaultValue)

    override fun putLong(
        key: String,
        value: Long,
    ) = preferences.edit { putLong(key, value) }

    override fun toMap(): Map<String, Any?> = preferences.all.toMap()
}
