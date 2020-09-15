package dev.lucasnlm.antimine.core.preferences

import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.core.control.ControlStyle

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

    fun useFlagAssistant(): Boolean
    fun useHapticFeedback(): Boolean
    fun areaSizeMultiplier(): Int
    fun useAnimations(): Boolean
    fun useQuestionMark(): Boolean
    fun isSoundEffectsEnabled(): Boolean
}
