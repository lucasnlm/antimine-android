package dev.lucasnlm.antimine.preferences

import android.view.ViewConfiguration
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.Minefield

class PreferencesRepository(
    private val preferencesManager: IPreferencesManager,
    private val defaultLongPressTimeout: Int,
) : IPreferencesRepository {
    init {
        migrateOldPreferences()
    }

    override fun hasCustomizations(): Boolean {
        return preferencesManager.getInt(PREFERENCE_AREA_SIZE, 50) != 50 ||
            preferencesManager.getInt(PREFERENCE_LONG_PRESS_TIMEOUT, ViewConfiguration.getLongPressTimeout()) !=
            ViewConfiguration.getLongPressTimeout() ||
            preferencesManager.getInt(PREFERENCE_SQUARE_RADIUS, 2) != 2
    }

    override fun reset() {
        preferencesManager.putBoolean(PREFERENCE_ASSISTANT, true)
        preferencesManager.putBoolean(PREFERENCE_VIBRATION, true)
        preferencesManager.putBoolean(PREFERENCE_ANIMATION, true)
        preferencesManager.putBoolean(PREFERENCE_QUESTION_MARK, false)
        preferencesManager.putBoolean(PREFERENCE_SOUND_EFFECTS, false)
        preferencesManager.putInt(PREFERENCE_SQUARE_RADIUS, 2)
        preferencesManager.putInt(PREFERENCE_AREA_SIZE, 50)
        preferencesManager.putInt(PREFERENCE_TOUCH_SENSIBILITY, 35)
        preferencesManager.putInt(PREFERENCE_LONG_PRESS_TIMEOUT, ViewConfiguration.getLongPressTimeout())
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

    override fun useFlagAssistant(): Boolean =
        preferencesManager.getBoolean(PREFERENCE_ASSISTANT, true)

    override fun setFlagAssistant(value: Boolean) {
        preferencesManager.putBoolean(PREFERENCE_ASSISTANT, value)
    }

    override fun useHapticFeedback(): Boolean =
        preferencesManager.getBoolean(PREFERENCE_VIBRATION, true)

    override fun setHapticFeedback(value: Boolean) {
        preferencesManager.putBoolean(PREFERENCE_VIBRATION, value)
    }

    override fun squareSizeMultiplier(): Int =
        preferencesManager.getInt(PREFERENCE_AREA_SIZE, 50)

    override fun setSquareMultiplier(value: Int) {
        preferencesManager.putInt(PREFERENCE_AREA_SIZE, value)
    }

    override fun useAnimations(): Boolean =
        preferencesManager.getBoolean(PREFERENCE_ANIMATION, true)

    override fun setNoGuessingAlgorithm(value: Boolean) {
        preferencesManager.putBoolean(PREFERENCE_NO_GUESSING, value)
    }

    override fun useNoGuessingAlgorithm(): Boolean =
        preferencesManager.getBoolean(PREFERENCE_NO_GUESSING, true)

    override fun useQuestionMark(): Boolean =
        preferencesManager.getBoolean(PREFERENCE_QUESTION_MARK, false)

    override fun setQuestionMark(value: Boolean) {
        preferencesManager.putBoolean(PREFERENCE_QUESTION_MARK, value)
    }

    override fun isSoundEffectsEnabled(): Boolean =
        preferencesManager.getBoolean(PREFERENCE_SOUND_EFFECTS, false)

    override fun setSoundEffectsEnabled(value: Boolean) {
        preferencesManager.putBoolean(PREFERENCE_SOUND_EFFECTS, value)
    }

    override fun touchSensibility(): Int =
        preferencesManager.getInt(PREFERENCE_TOUCH_SENSIBILITY, 35)

    override fun setTouchSensibility(sensibility: Int) {
        preferencesManager.putInt(PREFERENCE_TOUCH_SENSIBILITY, sensibility)
    }

    override fun showWindowsWhenFinishGame(): Boolean =
        preferencesManager.getBoolean(PREFERENCE_SHOW_WINDOWS, true)

    override fun controlStyle(): ControlStyle {
        val index = preferencesManager.getInt(PREFERENCE_CONTROL_STYLE, -1)
        return ControlStyle.values().getOrNull(index) ?: ControlStyle.Standard
    }

    override fun useControlStyle(controlStyle: ControlStyle) {
        preferencesManager.putInt(PREFERENCE_CONTROL_STYLE, controlStyle.ordinal)
    }

    override fun isFirstUse(): Boolean =
        preferencesManager.getBoolean(PREFERENCE_FIRST_USE, true)

    override fun completeFirstUse() {
        preferencesManager.putBoolean(PREFERENCE_FIRST_USE, false)
    }

    override fun isTutorialCompleted(): Boolean {
        return preferencesManager.getBoolean(PREFERENCE_TUTORIAL_COMPLETED, false)
    }

    override fun completeTutorial() {
        preferencesManager.putBoolean(PREFERENCE_TUTORIAL_COMPLETED, true)
    }

    override fun customLongPressTimeout(): Long =
        preferencesManager.getInt(PREFERENCE_LONG_PRESS_TIMEOUT, ViewConfiguration.getLongPressTimeout()).toLong()

    override fun setCustomLongPressTimeout(value: Long) {
        preferencesManager.putInt(PREFERENCE_LONG_PRESS_TIMEOUT, value.toInt())
    }

    override fun themeId(): Long =
        preferencesManager.getInt(PREFERENCE_CUSTOM_THEME, 0).toLong()

    override fun useTheme(themeId: Long) {
        preferencesManager.putInt(PREFERENCE_CUSTOM_THEME, themeId.toInt())
    }

    override fun updateStatsBase(statsBase: Int) {
        preferencesManager.putInt(PREFERENCE_STATS_BASE, statsBase)
    }

    override fun getStatsBase(): Int =
        preferencesManager.getInt(PREFERENCE_STATS_BASE, 0)

    override fun getUseCount(): Int =
        preferencesManager.getInt(PREFERENCE_USE_COUNT, 0)

    override fun incrementUseCount() {
        val current = preferencesManager.getInt(PREFERENCE_USE_COUNT, 0)
        preferencesManager.putInt(PREFERENCE_USE_COUNT, current + 1)
    }

    override fun incrementProgressiveValue() {
        val value = preferencesManager.getInt(PREFERENCE_PROGRESSIVE_VALUE, 0)
        preferencesManager.putInt(PREFERENCE_PROGRESSIVE_VALUE, value + 1)
    }

    override fun decrementProgressiveValue() {
        val value = preferencesManager.getInt(PREFERENCE_PROGRESSIVE_VALUE, 0)
        preferencesManager.putInt(PREFERENCE_PROGRESSIVE_VALUE, (value - 1).coerceAtLeast(0))
    }

    override fun getProgressiveValue(): Int =
        preferencesManager.getInt(PREFERENCE_PROGRESSIVE_VALUE, 0)

    override fun isRequestRatingEnabled(): Boolean =
        preferencesManager.getBoolean(PREFERENCE_REQUEST_RATING, true)

    override fun disableRequestRating() {
        preferencesManager.putBoolean(PREFERENCE_REQUEST_RATING, false)
    }

    private fun migrateOldPreferences() {
        // Migrate Double Click to the new Control settings
        if (preferencesManager.contains(PREFERENCE_OLD_DOUBLE_CLICK)) {
            if (preferencesManager.getBoolean(PREFERENCE_OLD_DOUBLE_CLICK, false)) {
                useControlStyle(ControlStyle.DoubleClick)
            }

            preferencesManager.removeKey(PREFERENCE_OLD_DOUBLE_CLICK)
        }

        // Migrate Large Area to Custom Area size
        if (preferencesManager.contains(PREFERENCE_OLD_LARGE_AREA)) {
            if (preferencesManager.getBoolean(PREFERENCE_OLD_LARGE_AREA, false)) {
                preferencesManager.putInt(PREFERENCE_AREA_SIZE, 63)
            } else {
                preferencesManager.putInt(PREFERENCE_AREA_SIZE, 50)
            }
            preferencesManager.removeKey(PREFERENCE_OLD_LARGE_AREA)
        }

        if (!preferencesManager.contains(PREFERENCE_AREA_SIZE)) {
            preferencesManager.putInt(PREFERENCE_AREA_SIZE, 50)
        }

        if (!preferencesManager.contains(PREFERENCE_LONG_PRESS_TIMEOUT)) {
            preferencesManager.putInt(PREFERENCE_LONG_PRESS_TIMEOUT, defaultLongPressTimeout)
        }

        if (preferencesManager.contains(PREFERENCE_FIRST_USE)) {
            preferencesManager.putBoolean(PREFERENCE_TUTORIAL_COMPLETED, true)
        }
    }

    override fun setPremiumFeatures(status: Boolean) {
        if (!preferencesManager.getBoolean(PREFERENCE_PREMIUM_FEATURES, false)) {
            preferencesManager.putBoolean(PREFERENCE_PREMIUM_FEATURES, status)
        }
    }

    override fun setShowSupport(show: Boolean) {
        preferencesManager.putBoolean(PREFERENCE_SHOW_SUPPORT, show)
    }

    override fun isPremiumEnabled(): Boolean =
        preferencesManager.getBoolean(PREFERENCE_PREMIUM_FEATURES, false)

    override fun showSupport(): Boolean {
        return preferencesManager.getBoolean(PREFERENCE_SHOW_SUPPORT, true)
    }

    override fun useHelp(): Boolean {
        return preferencesManager.getBoolean(PREFERENCE_USE_HELP, false)
    }

    override fun setHelp(value: Boolean) {
        preferencesManager.putBoolean(PREFERENCE_USE_HELP, value)
    }

    override fun squareRadius(): Int {
        return preferencesManager.getInt(PREFERENCE_SQUARE_RADIUS, 2)
    }

    override fun setSquareRadius(value: Int) {
        preferencesManager.putInt(PREFERENCE_SQUARE_RADIUS, value)
    }

    override fun getTips(): Int {
        return preferencesManager.getInt(PREFERENCE_TIPS, 5)
    }

    override fun setTips(tips: Int) {
        preferencesManager.putInt(PREFERENCE_TIPS, tips)
    }

    override fun getExtraTips(): Int {
        return preferencesManager.getInt(PREFERENCE_EXTRA_TIPS, 0)
    }

    override fun setExtraTips(tips: Int) {
        preferencesManager.putInt(PREFERENCE_EXTRA_TIPS, tips)
    }

    override fun openUsingSwitchControl(): Boolean {
        return preferencesManager.getBoolean(PREFERENCE_USE_OPEN_SWITCH_CONTROL, true)
    }

    override fun setSwitchControl(useOpen: Boolean) {
        preferencesManager.putBoolean(PREFERENCE_USE_OPEN_SWITCH_CONTROL, useOpen)
    }

    private companion object {
        private const val PREFERENCE_VIBRATION = "preference_vibration"
        private const val PREFERENCE_ASSISTANT = "preference_assistant"
        private const val PREFERENCE_ANIMATION = "preference_animation"
        private const val PREFERENCE_NO_GUESSING = "preference_no_guessing"
        private const val PREFERENCE_AREA_SIZE = "preference_area_size"
        private const val PREFERENCE_QUESTION_MARK = "preference_use_question_mark"
        private const val PREFERENCE_USE_HELP = "preference_use_help"
        private const val PREFERENCE_CONTROL_STYLE = "preference_control_style"
        private const val PREFERENCE_CUSTOM_THEME = "preference_custom_theme"
        private const val PREFERENCE_OLD_DOUBLE_CLICK = "preference_double_click_open"
        private const val PREFERENCE_CUSTOM_GAME_WIDTH = "preference_custom_game_width"
        private const val PREFERENCE_CUSTOM_GAME_HEIGHT = "preference_custom_game_height"
        private const val PREFERENCE_CUSTOM_GAME_MINES = "preference_custom_game_mines"
        private const val PREFERENCE_SOUND_EFFECTS = "preference_sound"
        private const val PREFERENCE_STATS_BASE = "preference_stats_base"
        private const val PREFERENCE_OLD_LARGE_AREA = "preference_large_area"
        private const val PREFERENCE_SQUARE_RADIUS = "preference_square_radius"
        private const val PREFERENCE_PROGRESSIVE_VALUE = "preference_progressive_value"
        private const val PREFERENCE_LONG_PRESS_TIMEOUT = "preference_long_press_timeout"
        private const val PREFERENCE_FIRST_USE = "preference_first_use"
        private const val PREFERENCE_TUTORIAL_COMPLETED = "preference_tutorial_completed"
        private const val PREFERENCE_USE_COUNT = "preference_use_count"
        private const val PREFERENCE_REQUEST_RATING = "preference_request_rating"
        private const val PREFERENCE_PREMIUM_FEATURES = "preference_premium_features"
        private const val PREFERENCE_SHOW_SUPPORT = "preference_show_support"
        private const val PREFERENCE_TIPS = "preference_current_tips"
        private const val PREFERENCE_EXTRA_TIPS = "preference_extra_tips"
        private const val PREFERENCE_SHOW_WINDOWS = "preference_show_windows"
        private const val PREFERENCE_USE_OPEN_SWITCH_CONTROL = "preference_use_open_switch_control"
        private const val PREFERENCE_TOUCH_SENSIBILITY = "preference_touch_sensibility"
    }
}
