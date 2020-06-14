package dev.lucasnlm.antimine.core.preferences

import android.content.Context
import androidx.preference.PreferenceManager
import dev.lucasnlm.antimine.common.level.models.Minefield
import javax.inject.Inject

class PreferencesInteractor @Inject constructor(
    private val context: Context
) {
    private val preferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getCustomMode() = Minefield(
        preferences.getInt(PREFERENCE_CUSTOM_GAME_WIDTH, 9),
        preferences.getInt(PREFERENCE_CUSTOM_GAME_HEIGHT, 9),
        preferences.getInt(PREFERENCE_CUSTOM_GAME_MINES, 9)
    )

    fun updateCustomMode(customMinefield: Minefield) {
        preferences.edit().apply {
            putInt(PREFERENCE_CUSTOM_GAME_WIDTH, customMinefield.width)
            putInt(PREFERENCE_CUSTOM_GAME_HEIGHT, customMinefield.height)
            putInt(PREFERENCE_CUSTOM_GAME_MINES, customMinefield.mines)
        }.apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean) = preferences.getBoolean(key, defaultValue)

    fun putBoolean(key: String, value: Boolean) = preferences.edit().putBoolean(key, value).apply()

    fun getInt(key: String, defaultValue: Int) = preferences.getInt(key, defaultValue)

    fun putInt(key: String, value: Int) = preferences.edit().putInt(key, value).apply()

    companion object {
        private const val PREFERENCE_CUSTOM_GAME_WIDTH = "preference_custom_game_width"
        private const val PREFERENCE_CUSTOM_GAME_HEIGHT = "preference_custom_game_height"
        private const val PREFERENCE_CUSTOM_GAME_MINES = "preference_custom_game_mines"
    }
}
