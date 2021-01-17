package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.IPreferencesRepository

class MockPreferencesRepository : IPreferencesRepository {
    private var customMinefield = Minefield(9, 9, 9)

    override fun hasCustomizations(): Boolean = true

    override fun reset() { }

    override fun customGameMode(): Minefield = customMinefield

    override fun updateCustomGameMode(minefield: Minefield) {
        customMinefield = minefield
    }

    override fun controlStyle(): ControlStyle = ControlStyle.Standard

    override fun useControlStyle(controlStyle: ControlStyle) { }

    override fun isFirstUse(): Boolean = false

    override fun completeFirstUse() { }

    override fun isTutorialCompleted(): Boolean = true

    override fun completeTutorial() { }

    override fun customLongPressTimeout(): Long = 400L

    override fun setCustomLongPressTimeout(value: Long) { }

    override fun themeId(): Long = 1L

    override fun useTheme(themeId: Long) { }

    override fun updateStatsBase(statsBase: Int) { }

    override fun getStatsBase(): Int = 0

    override fun getUseCount(): Int = 10

    override fun incrementUseCount() { }

    override fun incrementProgressiveValue() { }

    override fun decrementProgressiveValue() { }

    override fun getProgressiveValue(): Int = 0

    override fun isRequestRatingEnabled(): Boolean = false

    override fun disableRequestRating() { }

    override fun setPremiumFeatures(status: Boolean) { }

    override fun isPremiumEnabled(): Boolean = false

    override fun setShowSupport(show: Boolean) { }

    override fun showSupport(): Boolean = true

    override fun useHelp(): Boolean = false

    override fun setHelp(value: Boolean) { }

    override fun squareRadius(): Int = 2

    override fun setSquareRadius(value: Int) { }

    override fun getTips(): Int = 0

    override fun setTips(tips: Int) { }

    override fun getExtraTips(): Int = 5

    override fun setExtraTips(tips: Int) { }

    override fun openUsingSwitchControl(): Boolean = true

    override fun setSwitchControl(useOpen: Boolean) { }

    override fun useFlagAssistant(): Boolean = false

    override fun setFlagAssistant(value: Boolean) { }

    override fun useHapticFeedback(): Boolean = true

    override fun setHapticFeedback(value: Boolean) { }

    override fun squareSizeMultiplier(): Int = 50

    override fun setSquareMultiplier(value: Int) { }

    override fun useAnimations(): Boolean = false

    override fun setNoGuessingAlgorithm(value: Boolean) { }

    override fun useNoGuessingAlgorithm(): Boolean = true

    override fun useQuestionMark(): Boolean = false

    override fun setQuestionMark(value: Boolean) { }

    override fun isSoundEffectsEnabled(): Boolean = false

    override fun setSoundEffectsEnabled(value: Boolean) { }

    override fun touchSensibility(): Int = 35

    override fun setTouchSensibility(sensibility: Int) { }

    override fun showWindowsWhenFinishGame(): Boolean = true
}
