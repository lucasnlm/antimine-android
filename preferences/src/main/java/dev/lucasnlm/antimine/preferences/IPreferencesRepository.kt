package dev.lucasnlm.antimine.preferences

import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.Minefield

interface IPreferencesRepository {
    fun hasCustomizations(): Boolean
    fun reset()

    fun customGameMode(): Minefield
    fun updateCustomGameMode(minefield: Minefield)

    fun controlStyle(): ControlStyle
    fun useControlStyle(controlStyle: ControlStyle)

    fun isFirstUse(): Boolean
    fun completeFirstUse()

    fun isTutorialCompleted(): Boolean
    fun completeTutorial()

    fun customLongPressTimeout(): Long
    fun setCustomLongPressTimeout(value: Long)

    fun themeId(): Long
    fun useTheme(themeId: Long)

    fun updateStatsBase(statsBase: Int)
    fun getStatsBase(): Int

    fun getUseCount(): Int
    fun incrementUseCount()

    fun incrementProgressiveValue()
    fun decrementProgressiveValue()
    fun getProgressiveValue(): Int

    fun isRequestRatingEnabled(): Boolean
    fun disableRequestRating()

    fun setPremiumFeatures(status: Boolean)
    fun isPremiumEnabled(): Boolean

    fun setShowSupport(show: Boolean)
    fun showSupport(): Boolean

    fun useHelp(): Boolean
    fun setHelp(value: Boolean)

    fun squareRadius(): Int
    fun setSquareRadius(value: Int)

    fun getTips(): Int
    fun setTips(tips: Int)
    fun getExtraTips(): Int
    fun setExtraTips(tips: Int)

    fun openUsingSwitchControl(): Boolean
    fun setSwitchControl(useOpen: Boolean)

    fun useFlagAssistant(): Boolean
    fun setFlagAssistant(value: Boolean)

    fun useHapticFeedback(): Boolean
    fun setHapticFeedback(value: Boolean)

    fun squareSizeMultiplier(): Int
    fun setSquareMultiplier(value: Int)

    fun useAnimations(): Boolean

    fun setNoGuessingAlgorithm(value: Boolean)
    fun useNoGuessingAlgorithm(): Boolean

    fun useQuestionMark(): Boolean
    fun setQuestionMark(value: Boolean)

    fun isSoundEffectsEnabled(): Boolean
    fun setSoundEffectsEnabled(value: Boolean)

    fun touchSensibility(): Int
    fun setTouchSensibility(sensibility: Int)

    fun showWindowsWhenFinishGame(): Boolean
}
