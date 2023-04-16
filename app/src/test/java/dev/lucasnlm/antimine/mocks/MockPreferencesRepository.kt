package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.Minefield

class MockPreferencesRepository : PreferencesRepository {
    private var customMinefield = Minefield(9, 9, 9)

    override fun hasCustomizations(): Boolean = true

    override fun reset() {}

    override fun hasControlCustomizations(): Boolean = false

    override fun resetControls() {}

    override fun customGameMode(): Minefield = customMinefield

    override fun updateCustomGameMode(minefield: Minefield) {
        customMinefield = minefield
    }

    override fun forgetCustomSeed() {}

    override fun controlStyle(): ControlStyle = ControlStyle.Standard

    override fun hasCustomControlStyle(): Boolean = false

    override fun useControlStyle(controlStyle: ControlStyle) {}

    override fun isFirstUse(): Boolean = false

    override fun completeFirstUse() {}

    override fun isTutorialCompleted(): Boolean = true

    override fun setCompleteTutorial(value: Boolean) {}

    override fun customLongPressTimeout(): Long = 400L

    override fun setCustomLongPressTimeout(value: Long) {}

    override fun getDoubleClickTimeout(): Long = 400L

    override fun setDoubleClickTimeout(value: Long) {}

    override fun themeId(): Long = 1L

    override fun useTheme(themeId: Long) {}

    override fun skinId(): Long = 1L

    override fun useSkin(skinId: Long) {}

    override fun setPreferredLocale(locale: String) {}

    override fun getPreferredLocale(): String = "en"

    override fun updateStatsBase(statsBase: Int) {}

    override fun getStatsBase(): Int = 0

    override fun getUseCount(): Int = 10

    override fun incrementUseCount() {}

    override fun incrementProgressiveValue() {}

    override fun decrementProgressiveValue() {}

    override fun getProgressiveValue(): Int = 0

    override fun isRequestRatingEnabled(): Boolean = false

    override fun disableRequestRating() {}

    override fun setPremiumFeatures(status: Boolean) {}

    override fun isPremiumEnabled(): Boolean = false

    override fun setShowSupport(show: Boolean) {}

    override fun showSupport(): Boolean = true

    override fun useHelp(): Boolean = false

    override fun useSimonTathamAlgorithm(): Boolean = true

    override fun setSimonTathamAlgorithm(enabled: Boolean) {}

    override fun lastHelpUsed(): Long = 0L

    override fun refreshLastHelpUsed() {}

    override fun setHelp(value: Boolean) {}

    override fun getTips(): Int = 0

    override fun setTips(tips: Int) {}

    override fun getExtraTips(): Int = 5

    override fun setExtraTips(tips: Int) {}

    override fun getSwitchControlAction(): Action = Action.OpenTile

    override fun setSwitchControl(action: Action) {
        TODO("Not yet implemented")
    }

    override fun useFlagAssistant(): Boolean = false

    override fun setFlagAssistant(value: Boolean) {}

    override fun useHapticFeedback(): Boolean = true

    override fun setHapticFeedback(value: Boolean) {}

    override fun useAnimations(): Boolean = false

    override fun setAnimations(enabled: Boolean) {}

    override fun useQuestionMark(): Boolean = false

    override fun setQuestionMark(value: Boolean) {}

    override fun isSoundEffectsEnabled(): Boolean = false

    override fun setSoundEffectsEnabled(value: Boolean) {}

    override fun isMusicEnabled(): Boolean = false

    override fun setMusicEnabled(value: Boolean) {}

    override fun touchSensibility(): Int = 35

    override fun setTouchSensibility(sensibility: Int) {}

    override fun showWindowsWhenFinishGame(): Boolean = true

    override fun mustShowWindowsWhenFinishGame(enabled: Boolean) {}

    override fun openGameDirectly(): Boolean = false

    override fun setOpenGameDirectly(value: Boolean) {}

    override fun userId(): String? = null

    override fun setUserId(userId: String) {}

    override fun showTutorialDialog(): Boolean = false

    override fun setTutorialDialog(show: Boolean) {}

    override fun allowTapOnNumbers(): Boolean = true

    override fun setAllowTapOnNumbers(allow: Boolean) {}

    override fun getHapticFeedbackLevel(): Int = 100

    override fun setHapticFeedbackLevel(value: Int) {}

    override fun resetHapticFeedbackLevel() {}

    override fun showTutorialButton(): Boolean = true

    override fun setShowTutorialButton(value: Boolean) {}

    override fun showMusicBanner(): Boolean = true

    override fun setShowMusicBanner(value: Boolean) {}

    override fun lastMusicBanner(): Long = 0L

    override fun setLastMusicBanner(value: Long) {}

    override fun dimNumbers(): Boolean = false

    override fun setDimNumbers(value: Boolean) {}

    override fun setRequestDonation(request: Boolean) {}

    override fun requestDonation(): Boolean = true

    override fun letNumbersAutoFlag(): Boolean = true

    override fun setNumbersAutoFlag(allow: Boolean) {}

    override fun showTimer(): Boolean = true

    override fun setTimerVisible(visible: Boolean) {}

    override fun showContinueGame() = true

    override fun setContinueGameLabel(value: Boolean) {}

    override fun showNewThemesIcon(): Boolean = false

    override fun setNewThemesIcon(visible: Boolean) {}

    override fun exportData(): Map<String, Any?> = mapOf()

    override fun importData(data: Map<String, Any?>) {}

    override fun keepRequestPlayGames(): Boolean = false

    override fun setRequestPlayGames(showRequest: Boolean) {}

    override fun lastAppVersion(): Int = 0

    override fun setLastAppVersion(versionCode: Int) {}
}
