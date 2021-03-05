package dev.lucasnlm.external

abstract class IFeatureFlagManager {
    abstract val isGameHistoryEnabled: Boolean
    abstract val isRateUsEnabled: Boolean
    abstract val isInAppAdsEnabled: Boolean
    abstract val isGameplayAnalyticsEnabled: Boolean
    abstract val isGameOverAdEnabled: Boolean
    abstract val isAdsOnContinueEnabled: Boolean
    abstract val isAdsOnNewGameEnabled: Boolean
    abstract val isContinueGameEnabled: Boolean
    abstract val isRecyclerScrollEnabled: Boolean
    abstract val isFoos: Boolean
    abstract val isThemeTastingEnabled: Boolean
    abstract val minUsageToReview: Int

    abstract suspend fun refresh()
}
