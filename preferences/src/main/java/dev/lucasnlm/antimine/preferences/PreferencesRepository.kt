package dev.lucasnlm.antimine.preferences

import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.Minefield

interface PreferencesRepository {
    fun hasCustomizations(): Boolean

    fun reset()

    fun hasControlCustomizations(): Boolean

    fun resetControls()

    fun customGameMode(): Minefield

    fun updateCustomGameMode(minefield: Minefield)

    fun forgetCustomSeed()

    fun controlStyle(): ControlStyle

    fun hasCustomControlStyle(): Boolean

    fun useControlStyle(controlStyle: ControlStyle)

    fun isFirstUse(): Boolean

    fun completeFirstUse()

    fun isTutorialCompleted(): Boolean

    fun setCompleteTutorial(value: Boolean)

    fun showTutorialButton(): Boolean

    fun setShowTutorialButton(value: Boolean)

    fun showMusicBanner(): Boolean

    fun setShowMusicBanner(value: Boolean)

    fun lastMusicBanner(): Long

    fun setLastMusicBanner(value: Long)

    fun customLongPressTimeout(): Long

    fun setCustomLongPressTimeout(value: Long)

    fun getDoubleClickTimeout(): Long

    fun setDoubleClickTimeout(value: Long)

    fun themeId(): Long?

    fun useTheme(themeId: Long)

    fun skinId(): Long

    fun useSkin(skinId: Long)

    fun setPreferredLocale(locale: String)

    fun getPreferredLocale(): String?

    fun getUseCount(): Int

    fun incrementUseCount()

    fun incrementProgressiveValue()

    fun decrementProgressiveValue()

    fun getProgressiveValue(): Int

    fun isRequestRatingEnabled(): Boolean

    fun disableRequestRating()

    fun setPremiumFeatures(status: Boolean)

    fun isPremiumEnabled(): Boolean

    fun setRequestDonation(request: Boolean)

    fun requestDonation(): Boolean

    fun setShowSupport(show: Boolean)

    fun showSupport(): Boolean

    fun useHelp(): Boolean

    fun setHelp(value: Boolean)

    fun lastHelpUsed(): Long

    fun refreshLastHelpUsed()

    fun useSimonTathamAlgorithm(): Boolean

    fun setSimonTathamAlgorithm(enabled: Boolean)

    fun getTips(): Int

    fun setTips(tips: Int)

    fun getExtraTips(): Int

    fun setExtraTips(tips: Int)

    fun useFlagAssistant(): Boolean

    fun setFlagAssistant(value: Boolean)

    fun dimNumbers(): Boolean

    fun setDimNumbers(value: Boolean)

    fun useHapticFeedback(): Boolean

    fun setHapticFeedback(value: Boolean)

    fun getHapticFeedbackLevel(): Int

    fun setHapticFeedbackLevel(value: Int)

    fun resetHapticFeedbackLevel()

    fun useQuestionMark(): Boolean

    fun setQuestionMark(value: Boolean)

    fun isSoundEffectsEnabled(): Boolean

    fun setSoundEffectsEnabled(value: Boolean)

    fun isMusicEnabled(): Boolean

    fun setMusicEnabled(value: Boolean)

    fun touchSensibility(): Int

    fun setTouchSensibility(sensibility: Int)

    fun showWindowsWhenFinishGame(): Boolean

    fun mustShowWindowsWhenFinishGame(enabled: Boolean)

    fun openGameDirectly(): Boolean

    fun setOpenGameDirectly(value: Boolean)

    fun userId(): String?

    fun setUserId(userId: String)

    fun showTutorialDialog(): Boolean

    fun setTutorialDialog(show: Boolean)

    fun allowTapOnNumbers(): Boolean

    fun setAllowTapOnNumbers(allow: Boolean)

    fun letNumbersAutoFlag(): Boolean

    fun setNumbersAutoFlag(allow: Boolean)

    fun showTimer(): Boolean

    fun setTimerVisible(visible: Boolean)

    fun showContinueGame(): Boolean

    fun setContinueGameLabel(value: Boolean)

    fun showNewThemesIcon(): Boolean

    fun setNewThemesIcon(visible: Boolean)

    fun exportData(): Map<String, Any?>

    fun importData(data: Map<String, Any?>)

    fun keepRequestPlayGames(): Boolean

    fun setRequestPlayGames(showRequest: Boolean)

    fun lastAppVersion(): Int?

    fun setLastAppVersion(versionCode: Int)

    fun defaultSwitchButton(): Action

    fun setDefaultSwitchButton(action: Action)

    fun useImmersiveMode(): Boolean

    fun setImmersiveMode(enabled: Boolean)
}
