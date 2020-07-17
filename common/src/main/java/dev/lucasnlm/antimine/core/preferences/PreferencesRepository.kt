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

    fun controlStyle(): ControlStyle
    fun useControlStyle(controlStyle: ControlStyle)

    fun updateStatsBase(statsBase: Int)
    fun getStatsBase(): Int

    fun useFlagAssistant(): Boolean
    fun useHapticFeedback(): Boolean
    fun useLargeAreas(): Boolean
    fun useAnimations(): Boolean
    fun useQuestionMark(): Boolean
    fun isSoundEffectsEnabled(): Boolean
    fun useSolverAlgorithms(): Boolean
}

class PreferencesRepository(
    private val preferencesManager: IPreferencesManager
) : IPreferencesRepository {
    init {
        migrateOldPreferences()
    }

    override fun customGameMode(): Minefield = Minefield(
        preferencesManager.getInt(PREFERENCE_CUSTOM_GAME_WIDTH, 9),
        preferencesManager.getInt(PREFERENCE_CUSTOM_GAME_HEIGHT, 9),
        preferencesManager.getInt(PREFERENCE_CUSTOM_GAME_MINES, 9)
    )

    override fun updateCustomGameMode(minefield: Minefield) {
        preferencesManager.apply {
            putInt(PREFERENCE_CUSTOM_GAME_WIDTH, minefield.width)
            putInt(PREFERENCE_CUSTOM_GAME_HEIGHT, minefield.height)
            putInt(PREFERENCE_CUSTOM_GAME_MINES, minefield.mines)
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        preferencesManager.getBoolean(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) =
        preferencesManager.putBoolean(key, value)

    override fun getInt(key: String, defaultValue: Int): Int =
        preferencesManager.getInt(key, defaultValue)

    override fun putInt(key: String, value: Int) =
        preferencesManager.putInt(key, value)

    override fun useFlagAssistant(): Boolean =
        getBoolean(PREFERENCE_ASSISTANT, true)

    override fun useHapticFeedback(): Boolean =
        getBoolean(PREFERENCE_VIBRATION, true)

    override fun useLargeAreas(): Boolean =
        getBoolean(PREFERENCE_USE_LARGE_TILE, false)

    override fun useAnimations(): Boolean =
        getBoolean(PREFERENCE_ANIMATION, true)

    override fun useQuestionMark(): Boolean =
        getBoolean(PREFERENCE_QUESTION_MARK, false)

    override fun isSoundEffectsEnabled(): Boolean =
        getBoolean(PREFERENCE_SOUND_EFFECTS, false)

    override fun useSolverAlgorithms(): Boolean =
        getBoolean(PREFERENCE_USE_SOLVER_ALGORITHMS, false)

    override fun controlStyle(): ControlStyle {
        val index = getInt(PREFERENCE_CONTROL_STYLE, -1)
        return ControlStyle.values().getOrNull(index) ?: ControlStyle.Standard
    }

    override fun useControlStyle(controlStyle: ControlStyle) {
        putInt(PREFERENCE_CONTROL_STYLE, controlStyle.ordinal)
    }

    override fun updateStatsBase(statsBase: Int) {
        putInt(PREFERENCE_STATS_BASE, statsBase)
    }

    override fun getStatsBase(): Int =
        getInt(PREFERENCE_STATS_BASE, 0)

    private fun migrateOldPreferences() {
        if (preferencesManager.contains(PREFERENCE_OLD_DOUBLE_CLICK)) {
            if (getBoolean(PREFERENCE_OLD_DOUBLE_CLICK, false)) {
                useControlStyle(ControlStyle.DoubleClick)
            }

            preferencesManager.removeKey(PREFERENCE_OLD_DOUBLE_CLICK)
        }
    }

    private companion object {
        private const val PREFERENCE_VIBRATION = "preference_vibration"
        private const val PREFERENCE_ASSISTANT = "preference_assistant"
        private const val PREFERENCE_ANIMATION = "preference_animation"
        private const val PREFERENCE_USE_LARGE_TILE = "preference_large_area"
        private const val PREFERENCE_QUESTION_MARK = "preference_use_question_mark"
        private const val PREFERENCE_CONTROL_STYLE = "preference_control_style"
        private const val PREFERENCE_OLD_DOUBLE_CLICK = "preference_double_click_open"
        private const val PREFERENCE_CUSTOM_GAME_WIDTH = "preference_custom_game_width"
        private const val PREFERENCE_CUSTOM_GAME_HEIGHT = "preference_custom_game_height"
        private const val PREFERENCE_CUSTOM_GAME_MINES = "preference_custom_game_mines"
        private const val PREFERENCE_SOUND_EFFECTS = "preference_sound"
        private const val PREFERENCE_STATS_BASE = "preference_stats_base"
        private const val PREFERENCE_USE_SOLVER_ALGORITHMS = "preference_use_solver_algorithms"
    }
}
