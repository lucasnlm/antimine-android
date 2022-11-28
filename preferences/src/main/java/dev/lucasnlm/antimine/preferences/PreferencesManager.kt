package dev.lucasnlm.antimine.preferences

import android.content.Context
import androidx.preference.PreferenceManager

class PreferencesManager(
    private val context: Context,
) : IPreferencesManager {
    private val preferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun getBoolean(key: String, defaultValue: Boolean) = preferences.getBoolean(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) = preferences.edit().putBoolean(key, value).apply()

    override fun getInt(key: String, defaultValue: Int) = preferences.getInt(key, defaultValue)

    override fun getLongOrNull(key: String): Long? {
        return if (preferences.contains(key)) {
            preferences.getLong(key, -1)
        } else {
            null
        }
    }

    override fun putInt(key: String, value: Int) = preferences.edit().putInt(key, value).apply()

    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun putString(key: String, value: String) = preferences.edit().putString(key, value).apply()

    override fun contains(key: String): Boolean = preferences.contains(key)

    override fun removeKey(key: String) = preferences.edit().remove(key).apply()

    override fun getLong(key: String, defaultValue: Long): Long = preferences.getLong(key, defaultValue)

    override fun putLong(key: String, value: Long) = preferences.edit().putLong(key, value).apply()
}
