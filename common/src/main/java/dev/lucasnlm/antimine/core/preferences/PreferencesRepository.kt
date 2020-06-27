package dev.lucasnlm.antimine.core.preferences

import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.core.control.ControlStyle

interface IPreferencesRepository {
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun getInt(key: String, defaultValue: Int): Int
    fun putBoolean(key: String, value: Boolean)
    fun putInt(key: String, value: Int)

    fun customGameMode(): Minefield
    fun updateCustomGameMode(minefield: Minefield)

    fun controlType(): ControlStyle
    fun useControlType(controlStyle: ControlStyle)

    fun useFlagAssistant(): Boolean
    fun useHapticFeedback(): Boolean
    fun useLargeAreas(): Boolean
    fun useAnimations(): Boolean
}

class PreferencesRepository(
    private val preferencesManager: PreferencesManager
) : IPreferencesRepository {
    init {
        migrateOldPreferences()
    }

    override fun customGameMode(): Minefield =
        preferencesManager.getCustomMode()

    override fun updateCustomGameMode(minefield: Minefield) =
        preferencesManager.updateCustomMode(minefield)

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        preferencesManager.getBoolean(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) =
        preferencesManager.putBoolean(key, value)

    override fun getInt(key: String, defaultValue: Int): Int =
        preferencesManager.getInt(key, defaultValue)

    override fun putInt(key: String, value: Int) =
        preferencesManager.putInt(key, value)

    override fun useFlagAssistant(): Boolean =
        getBoolean("preference_assistant", true)

    override fun useHapticFeedback(): Boolean =
        getBoolean("preference_vibration", true)

    override fun useLargeAreas(): Boolean =
        getBoolean("preference_large_area", false)

    override fun useAnimations(): Boolean =
        getBoolean("preference_animation", true)

    override fun controlType(): ControlStyle {
        val index = getInt("preference_control_type", -1)
        return ControlStyle.values().getOrNull(index) ?: ControlStyle.Standard
    }

    override fun useControlType(controlStyle: ControlStyle) {
        putInt("preference_control_type", controlStyle.ordinal)
    }

    private fun migrateOldPreferences() {
        if (getBoolean("preference_double_click_open", false)) {
            useControlType(ControlStyle.DoubleClick)
            preferencesManager.removeKey("preference_double_click_open")
        }
    }
}
