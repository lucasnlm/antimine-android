package dev.lucasnlm.antimine.preferences

import android.os.Build
import android.view.ViewConfiguration
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.Minefield

class PreferencesRepositoryImpl(
    private val preferencesManager: PreferencesManager,
    private val defaultLongPressTimeout: Int,
) : PreferencesRepository {
    init {
        migrateOldPreferences()
    }

    private val listOfControlCustoms =
        listOf(
            PreferenceKeys.PREFERENCE_TOUCH_SENSIBILITY,
            PreferenceKeys.PREFERENCE_LONG_PRESS_TIMEOUT,
            PreferenceKeys.PREFERENCE_DOUBLE_CLICK_TIMEOUT,
        )

    private val listOfSettingsCustoms =
        listOf(
            PreferenceKeys.PREFERENCE_ASSISTANT,
            PreferenceKeys.PREFERENCE_QUESTION_MARK,
            PreferenceKeys.PREFERENCE_USE_HINT,
            PreferenceKeys.PREFERENCE_SOUND_EFFECTS,
            PreferenceKeys.PREFERENCE_SHOW_WINDOWS,
            PreferenceKeys.PREFERENCE_OPEN_DIRECTLY,
            PreferenceKeys.PREFERENCE_ALLOW_TAP_NUMBER,
            PreferenceKeys.PREFERENCE_SHOW_CLOCK,
            PreferenceKeys.PREFERENCE_DIM_NUMBERS,
            PreferenceKeys.PREFERENCE_LET_NUMBERS_AUTO_FLAG,
        )

    private fun longPressTimeout() = ViewConfiguration.getLongPressTimeout()

    override fun hasCustomizations(): Boolean {
        val vibrationDisabled = preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_VIBRATION, true)
        return listOfSettingsCustoms.any { preferencesManager.contains(it) } || !vibrationDisabled
    }

    override fun hasControlCustomizations(): Boolean {
        return listOfControlCustoms.fold(false) { acc, current ->
            acc || preferencesManager.contains(current)
        }
    }

    override fun resetControls() {
        listOfControlCustoms.forEach { preferencesManager.removeKey(it) }
    }

    override fun reset() {
        listOfSettingsCustoms.forEach { preferencesManager.removeKey(it) }
    }

    override fun forgetCustomSeed() {
        preferencesManager.removeKey(PreferenceKeys.PREFERENCE_CUSTOM_GAME_SEED)
    }

    override fun customGameMode(): Minefield =
        with(preferencesManager) {
            Minefield(
                getInt(PreferenceKeys.PREFERENCE_CUSTOM_GAME_WIDTH, 9),
                getInt(PreferenceKeys.PREFERENCE_CUSTOM_GAME_HEIGHT, 9),
                getInt(PreferenceKeys.PREFERENCE_CUSTOM_GAME_MINES, 9),
                getLongOrNull(PreferenceKeys.PREFERENCE_CUSTOM_GAME_SEED),
            )
        }

    override fun updateCustomGameMode(minefield: Minefield) {
        preferencesManager.apply {
            putInt(PreferenceKeys.PREFERENCE_CUSTOM_GAME_WIDTH, minefield.width)
            putInt(PreferenceKeys.PREFERENCE_CUSTOM_GAME_HEIGHT, minefield.height)
            putInt(PreferenceKeys.PREFERENCE_CUSTOM_GAME_MINES, minefield.mines)
            if (minefield.seed != null) {
                putLong(PreferenceKeys.PREFERENCE_CUSTOM_GAME_SEED, minefield.seed)
            } else {
                removeKey(PreferenceKeys.PREFERENCE_CUSTOM_GAME_SEED)
            }
        }
    }

    override fun useFlagAssistant(): Boolean = preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_ASSISTANT, true)

    override fun setFlagAssistant(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_ASSISTANT, value)
    }

    override fun useHapticFeedback(): Boolean = preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_VIBRATION, true)

    override fun setHapticFeedback(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_VIBRATION, value)
    }

    override fun getHapticFeedbackLevel(): Int {
        return preferencesManager.getInt(PreferenceKeys.PREFERENCE_VIBRATION_LEVEL, 100)
    }

    override fun setHapticFeedbackLevel(value: Int) {
        val newValue = value.coerceIn(0, 200)
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_VIBRATION_LEVEL, newValue)
    }

    override fun resetHapticFeedbackLevel() {
        preferencesManager.removeKey(PreferenceKeys.PREFERENCE_VIBRATION_LEVEL)
    }

    override fun useQuestionMark(): Boolean =
        preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_QUESTION_MARK, false)

    override fun setQuestionMark(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_QUESTION_MARK, value)
    }

    override fun isSoundEffectsEnabled(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_SOUND_EFFECTS, true)
    }

    override fun setSoundEffectsEnabled(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_SOUND_EFFECTS, value)
    }

    override fun isMusicEnabled(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_MUSIC, true)
    }

    override fun setMusicEnabled(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_MUSIC, value)
    }

    override fun touchSensibility(): Int = preferencesManager.getInt(PreferenceKeys.PREFERENCE_TOUCH_SENSIBILITY, 5)

    override fun setTouchSensibility(sensibility: Int) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_TOUCH_SENSIBILITY, sensibility)
    }

    override fun showWindowsWhenFinishGame(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_SHOW_WINDOWS, true)
    }

    override fun mustShowWindowsWhenFinishGame(enabled: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_SHOW_WINDOWS, enabled)
    }

    override fun userId(): String? {
        return preferencesManager.getString(PreferenceKeys.PREFERENCE_USER_ID)
    }

    override fun setUserId(userId: String) {
        if (userId.isBlank()) {
            preferencesManager.removeKey(userId)
        } else {
            preferencesManager.putString(PreferenceKeys.PREFERENCE_USER_ID, userId)
        }
    }

    override fun controlStyle(): ControlStyle {
        val index = preferencesManager.getInt(PreferenceKeys.PREFERENCE_CONTROL_STYLE, -1)
        return ControlStyle.values().getOrNull(index) ?: ControlStyle.SwitchMarkOpen
    }

    override fun hasCustomControlStyle(): Boolean {
        return preferencesManager.contains(PreferenceKeys.PREFERENCE_CONTROL_STYLE)
    }

    override fun useControlStyle(controlStyle: ControlStyle) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_CONTROL_STYLE, controlStyle.ordinal)
    }

    override fun isFirstUse(): Boolean = preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_FIRST_USE, true)

    override fun completeFirstUse() {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_FIRST_USE, false)
    }

    override fun isTutorialCompleted(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_TUTORIAL_COMPLETED, false)
    }

    override fun setCompleteTutorial(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_TUTORIAL_COMPLETED, value)
    }

    override fun customLongPressTimeout(): Long =
        preferencesManager.getInt(PreferenceKeys.PREFERENCE_LONG_PRESS_TIMEOUT, longPressTimeout()).toLong()

    override fun setCustomLongPressTimeout(value: Long) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_LONG_PRESS_TIMEOUT, value.toInt())
    }

    override fun getDoubleClickTimeout(): Long {
        return preferencesManager.getInt(PreferenceKeys.PREFERENCE_DOUBLE_CLICK_TIMEOUT, 250).toLong()
    }

    override fun setDoubleClickTimeout(value: Long) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_DOUBLE_CLICK_TIMEOUT, value.toInt())
    }

    override fun themeId(): Long? = preferencesManager.getIntOrNull(PreferenceKeys.PREFERENCE_CUSTOM_THEME)?.toLong()

    override fun useTheme(themeId: Long) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_CUSTOM_THEME, themeId.toInt())
    }

    override fun skinId(): Long {
        return preferencesManager.getInt(PreferenceKeys.PREFERENCE_CUSTOM_SKIN, 0).toLong()
    }

    override fun useSkin(skinId: Long) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_CUSTOM_SKIN, skinId.toInt())
    }

    override fun setPreferredLocale(locale: String) {
        preferencesManager.putString(PreferenceKeys.PREFERENCE_LOCALE, locale)
    }

    override fun getPreferredLocale(): String? {
        return preferencesManager.getString(PreferenceKeys.PREFERENCE_LOCALE)
    }

    override fun getUseCount(): Int = preferencesManager.getInt(PreferenceKeys.PREFERENCE_USE_COUNT, 0)

    override fun incrementUseCount() {
        val current = preferencesManager.getInt(PreferenceKeys.PREFERENCE_USE_COUNT, 0)
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_USE_COUNT, current + 1)
    }

    override fun incrementProgressiveValue() {
        val value = preferencesManager.getInt(PreferenceKeys.PREFERENCE_PROGRESSIVE_VALUE, 0)
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_PROGRESSIVE_VALUE, value + 1)
    }

    override fun decrementProgressiveValue() {
        val value = preferencesManager.getInt(PreferenceKeys.PREFERENCE_PROGRESSIVE_VALUE, 0)
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_PROGRESSIVE_VALUE, (value - 1).coerceAtLeast(0))
    }

    override fun getProgressiveValue(): Int = preferencesManager.getInt(PreferenceKeys.PREFERENCE_PROGRESSIVE_VALUE, 0)

    override fun isRequestRatingEnabled(): Boolean =
        preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_REQUEST_RATING, true)

    override fun disableRequestRating() {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_REQUEST_RATING, false)
    }

    private fun migrateOldPreferences() {
        // Migrate Double Click to the new Control settings
        if (preferencesManager.contains(PreferenceKeys.PREFERENCE_OLD_DOUBLE_CLICK)) {
            if (preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_OLD_DOUBLE_CLICK, false)) {
                useControlStyle(ControlStyle.DoubleClick)
            }

            preferencesManager.removeKey(PreferenceKeys.PREFERENCE_OLD_DOUBLE_CLICK)
        }

        if (!preferencesManager.contains(PreferenceKeys.PREFERENCE_LONG_PRESS_TIMEOUT)) {
            preferencesManager.putInt(PreferenceKeys.PREFERENCE_LONG_PRESS_TIMEOUT, defaultLongPressTimeout)
        }

        if (preferencesManager.contains(PreferenceKeys.PREFERENCE_FIRST_USE)) {
            preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_TUTORIAL_COMPLETED, true)
        }
    }

    override fun setPremiumFeatures(status: Boolean) {
        if (!preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_PREMIUM_FEATURES, false)) {
            preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_PREMIUM_FEATURES, status)
        }
    }

    override fun setShowSupport(show: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_SHOW_SUPPORT, show)
    }

    override fun isPremiumEnabled(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_PREMIUM_FEATURES, false)
    }

    override fun showSupport(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_SHOW_SUPPORT, true)
    }

    override fun useHelp(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_USE_HINT, true)
    }

    override fun setHelp(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_USE_HINT, value)
    }

    override fun lastHelpUsed(): Long {
        return preferencesManager.getLong(PreferenceKeys.PREFERENCE_LAST_HELP_USED, 0L)
    }

    override fun refreshLastHelpUsed() {
        preferencesManager.putLong(PreferenceKeys.PREFERENCE_LAST_HELP_USED, System.currentTimeMillis())
    }

    override fun useSimonTathamAlgorithm(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_SIMON_TATHAM_ALGORITHM, true)
    }

    override fun setSimonTathamAlgorithm(enabled: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_SIMON_TATHAM_ALGORITHM, enabled)
    }

    override fun getTips(): Int {
        return preferencesManager.getInt(PreferenceKeys.PREFERENCE_HINTS, 5)
    }

    override fun setTips(tips: Int) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_HINTS, tips)
    }

    override fun getExtraTips(): Int {
        return preferencesManager.getInt(PreferenceKeys.PREFERENCE_EXTRA_HINTS, 0)
    }

    override fun setExtraTips(tips: Int) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_EXTRA_HINTS, tips)
    }

    override fun openGameDirectly(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_OPEN_DIRECTLY, false)
    }

    override fun setOpenGameDirectly(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_OPEN_DIRECTLY, value)
    }

    override fun showTutorialDialog(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_TUTORIAL_DIALOG, true)
    }

    override fun setTutorialDialog(show: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_TUTORIAL_DIALOG, show)
    }

    override fun allowTapOnNumbers(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_ALLOW_TAP_NUMBER, true)
    }

    override fun setAllowTapOnNumbers(allow: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_ALLOW_TAP_NUMBER, allow)
    }

    override fun showTutorialButton(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_SHOULD_SHOW_TUTORIAL_BUTTON, true)
    }

    override fun setShowTutorialButton(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_SHOULD_SHOW_TUTORIAL_BUTTON, value)
    }

    override fun showMusicBanner(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_MUSIC_BANNER, true)
    }

    override fun setShowMusicBanner(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_MUSIC_BANNER, value)
    }

    override fun lastMusicBanner(): Long {
        return preferencesManager.getLong(PreferenceKeys.PREFERENCE_MUSIC_BANNER_LAST, 0L)
    }

    override fun setLastMusicBanner(value: Long) {
        preferencesManager.putLong(PreferenceKeys.PREFERENCE_MUSIC_BANNER_LAST, value)
    }

    override fun dimNumbers(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_DIM_NUMBERS, true)
    }

    override fun setDimNumbers(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_DIM_NUMBERS, value)
    }

    override fun setRequestDonation(request: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_REQUEST_DONATION, request)
    }

    override fun requestDonation(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_REQUEST_DONATION, true)
    }

    override fun letNumbersAutoFlag(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_LET_NUMBERS_AUTO_FLAG, true)
    }

    override fun setNumbersAutoFlag(allow: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_LET_NUMBERS_AUTO_FLAG, allow)
    }

    override fun showTimer(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_SHOW_CLOCK, true)
    }

    override fun setTimerVisible(visible: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_SHOW_CLOCK, visible)
    }

    override fun showContinueGame(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_SHOW_CONTINUE, false)
    }

    override fun setContinueGameLabel(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_SHOW_CONTINUE, value)
    }

    override fun showNewThemesIcon(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_NEW_THEMES_ICON, true)
    }

    override fun setNewThemesIcon(visible: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_NEW_THEMES_ICON, visible)
    }

    override fun exportData(): Map<String, Any?> {
        return preferencesManager.toMap()
    }

    override fun importData(data: Map<String, Any?>) {
        val wasPremium = preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_PREMIUM_FEATURES, false)

        preferencesManager.clear()

        data.filter {
            it.key != PreferenceKeys.PREFERENCE_PREMIUM_FEATURES
        }.forEach { (key, value) ->
            when (value) {
                null -> {
                    // Ignore
                }
                is Long -> {
                    preferencesManager.putLong(key, value)
                }
                is Int -> {
                    preferencesManager.putInt(key, value)
                }
                is String -> {
                    preferencesManager.putString(key, value)
                }
                is Boolean -> {
                    preferencesManager.putBoolean(key, value)
                }
            }
        }

        if (wasPremium) {
            preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_PREMIUM_FEATURES, true)
        }
    }

    override fun keepRequestPlayGames(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_REQUEST_PLAY_GAMES, true)
    }

    override fun setRequestPlayGames(showRequest: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_REQUEST_PLAY_GAMES, showRequest)
    }

    override fun lastAppVersion(): Int? {
        return preferencesManager.getIntOrNull(PreferenceKeys.PREFERENCE_LAST_VERSION)
    }

    override fun setLastAppVersion(versionCode: Int) {
        if (versionCode > 0) {
            preferencesManager.putInt(PreferenceKeys.PREFERENCE_LAST_VERSION, versionCode)
        }
    }

    override fun defaultSwitchButton(): Action {
        preferencesManager.getInt(PreferenceKeys.PREFERENCE_DEFAULT_SWITCH_BUTTON, 0).let {
            return Action.values().getOrNull(it) ?: Action.OpenTile
        }
    }

    override fun setDefaultSwitchButton(action: Action) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_DEFAULT_SWITCH_BUTTON, action.ordinal)
    }

    override fun useImmersiveMode(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_USE_IMMERSIVE_MODE, false)
    }

    override fun setImmersiveMode(enabled: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_USE_IMMERSIVE_MODE, enabled)
    }
}
