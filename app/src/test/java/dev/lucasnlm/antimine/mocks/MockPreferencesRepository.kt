package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.Minefield

class MockPreferencesRepository : PreferencesRepository {
    private var customMinefield = Minefield(9, 9, 9)

    override fun hasCustomizations(): Boolean = true

    override fun reset() {
        // Not implemented
    }

    override fun hasControlCustomizations(): Boolean = false

    override fun resetControls() {
        // Not implemented
    }

    override fun customGameMode(): Minefield = customMinefield

    override fun updateCustomGameMode(minefield: Minefield) {
        customMinefield = minefield
    }

    override fun forgetCustomSeed() {
        // Not implemented
    }

    override fun controlStyle(): ControlStyle = ControlStyle.Standard

    override fun hasCustomControlStyle(): Boolean = false

    override fun useControlStyle(controlStyle: ControlStyle) {
        // Not implemented
    }

    override fun isFirstUse(): Boolean = false

    override fun completeFirstUse() {
        // Not implemented
    }

    override fun isTutorialCompleted(): Boolean = true

    override fun setCompleteTutorial(value: Boolean) {
        // Not implemented
    }

    override fun customLongPressTimeout(): Long = 400L

    override fun setCustomLongPressTimeout(value: Long) {
        // Not implemented
    }

    override fun getDoubleClickTimeout(): Long = 400L

    override fun setDoubleClickTimeout(value: Long) {
        // Not implemented
    }

    override fun themeId(): Long = 1L

    override fun useTheme(themeId: Long) {
        // Not implemented
    }

    override fun skinId(): Long = 1L

    override fun useSkin(skinId: Long) {
        // Not implemented
    }

    override fun setPreferredLocale(locale: String) {
        // Not implemented
    }

    override fun getPreferredLocale(): String = "en"

    override fun getUseCount(): Int = 10

    override fun incrementUseCount() {
        // Not implemented
    }

    override fun incrementProgressiveValue() {
        // Not implemented
    }

    override fun decrementProgressiveValue() {
        // Not implemented
    }

    override fun getProgressiveValue(): Int = 0

    override fun isRequestRatingEnabled(): Boolean = false

    override fun disableRequestRating() {
        // Not implemented
    }

    override fun setPremiumFeatures(status: Boolean) {
        // Not implemented
    }

    override fun isPremiumEnabled(): Boolean = false

    override fun setShowSupport(show: Boolean) {
        // Not implemented
    }

    override fun showSupport(): Boolean = true

    override fun useHelp(): Boolean = false

    override fun useSimonTathamAlgorithm(): Boolean = true

    override fun setSimonTathamAlgorithm(enabled: Boolean) {
        // Not implemented
    }

    override fun lastHelpUsed(): Long = 0L

    override fun refreshLastHelpUsed() {
        // Not implemented
    }

    override fun setHelp(value: Boolean) {
        // Not implemented
    }

    override fun getTips(): Int = 0

    override fun setTips(tips: Int) {
        // Not implemented
    }

    override fun getExtraTips(): Int = 5

    override fun setExtraTips(tips: Int) {
        // Not implemented
    }

    override fun useFlagAssistant(): Boolean = false

    override fun setFlagAssistant(value: Boolean) {
        // Not implemented
    }

    override fun useHapticFeedback(): Boolean = true

    override fun setHapticFeedback(value: Boolean) {
        // Not implemented
    }

    override fun useQuestionMark(): Boolean = false

    override fun setQuestionMark(value: Boolean) {
        // Not implemented
    }

    override fun isSoundEffectsEnabled(): Boolean = false

    override fun setSoundEffectsEnabled(value: Boolean) {
        // Not implemented
    }

    override fun isMusicEnabled(): Boolean = false

    override fun setMusicEnabled(value: Boolean) {
        // Not implemented
    }

    override fun touchSensibility(): Int = 35

    override fun setTouchSensibility(sensibility: Int) {
        // Not implemented
    }

    override fun showWindowsWhenFinishGame(): Boolean = true

    override fun mustShowWindowsWhenFinishGame(enabled: Boolean) {
        // Not implemented
    }

    override fun openGameDirectly(): Boolean = false

    override fun setOpenGameDirectly(value: Boolean) {
        // Not implemented
    }

    override fun userId(): String? = null

    override fun setUserId(userId: String) {
        // Not implemented
    }

    override fun showTutorialDialog(): Boolean = false

    override fun setTutorialDialog(show: Boolean) {
        // Not implemented
    }

    override fun allowTapOnNumbers(): Boolean = true

    override fun setAllowTapOnNumbers(allow: Boolean) {
        // Not implemented
    }

    override fun getHapticFeedbackLevel(): Int = 100

    override fun setHapticFeedbackLevel(value: Int) {
        // Not implemented
    }

    override fun resetHapticFeedbackLevel() {
        // Not implemented
    }

    override fun showTutorialButton(): Boolean = true

    override fun setShowTutorialButton(value: Boolean) {
        // Not implemented
    }

    override fun showMusicBanner(): Boolean = true

    override fun setShowMusicBanner(value: Boolean) {
        // Not implemented
    }

    override fun lastMusicBanner(): Long = 0L

    override fun setLastMusicBanner(value: Long) {
        // Not implemented
    }

    override fun dimNumbers(): Boolean = false

    override fun setDimNumbers(value: Boolean) {
        // Not implemented
    }

    override fun setRequestDonation(request: Boolean) {
        // Not implemented
    }

    override fun requestDonation(): Boolean = true

    override fun letNumbersAutoFlag(): Boolean = true

    override fun setNumbersAutoFlag(allow: Boolean) {
        // Not implemented
    }

    override fun showTimer(): Boolean = true

    override fun setTimerVisible(visible: Boolean) {
        // Not implemented
    }

    override fun showContinueGame() = true

    override fun setContinueGameLabel(value: Boolean) {
        // Not implemented
    }

    override fun showNewThemesIcon(): Boolean = false

    override fun setNewThemesIcon(visible: Boolean) {
        // Not implemented
    }

    override fun exportData(): Map<String, Any?> = mapOf()

    override fun importData(data: Map<String, Any?>) {
        // Not implemented
    }

    override fun keepRequestPlayGames(): Boolean = false

    override fun setRequestPlayGames(showRequest: Boolean) {
        // Not implemented
    }

    override fun lastAppVersion(): Int = 0

    override fun setLastAppVersion(versionCode: Int) {
        // Not implemented
    }

    override fun defaultSwitchButton(): Action = Action.SwitchMark

    override fun setDefaultSwitchButton(action: Action) {
        // Not implemented
    }

    override fun useImmersiveMode(): Boolean {
        return false
    }

    override fun setImmersiveMode(enabled: Boolean) {
        // Not implemented
    }
}
