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

    override fun putInt(key: String, value: Int) = preferences.edit().putInt(key, value).apply()

    override fun contains(key: String): Boolean = preferences.contains(key)

    override fun removeKey(key: String) = preferences.edit().remove(key).apply()
}
