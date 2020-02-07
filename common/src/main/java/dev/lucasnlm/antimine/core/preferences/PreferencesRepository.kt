package dev.lucasnlm.antimine.core.preferences

import dev.lucasnlm.antimine.common.level.data.LevelSetup

interface IPreferencesRepository {
    fun customGameMode(): LevelSetup
    fun updateCustomGameMode(levelSetup: LevelSetup)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun getInt(key: String, defaultValue: Int): Int
    fun putBoolean(key: String, value: Boolean)
    fun putInt(key: String, value: Int)

    fun useFlagAssistant(): Boolean
    fun useHapticFeedback(): Boolean
    fun useLargeAreas(): Boolean
}

class PreferencesRepository(
    private val preferencesInteractor: PreferencesInteractor
) : IPreferencesRepository {

    override fun customGameMode(): LevelSetup =
        preferencesInteractor.getCustomMode()

    override fun updateCustomGameMode(levelSetup: LevelSetup) =
        preferencesInteractor.updateCustomMode(levelSetup)

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        preferencesInteractor.getBoolean(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) =
        preferencesInteractor.putBoolean(key, value)

    override fun getInt(key: String, defaultValue: Int): Int =
        preferencesInteractor.getInt(key, defaultValue)

    override fun putInt(key: String, value: Int) =
        preferencesInteractor.putInt(key, value)

    override fun useFlagAssistant(): Boolean =
        getBoolean("preference_assistant", true)

    override fun useHapticFeedback(): Boolean =
        getBoolean("preference_vibration", true)

    override fun useLargeAreas(): Boolean =
        getBoolean("preference_large_area", false)

}
