package dev.lucasnlm.external

abstract class IFeatureFlagManager {
    abstract val isGameHistoryEnabled: Boolean
    abstract val isRateUsEnabled: Boolean
    abstract val isInAppAdsEnabled: Boolean
    abstract val isGameplayAnalyticsEnabled: Boolean
    abstract val isGameOverAdEnabled: Boolean
    abstract val isAdsOnContinueEnabled: Boolean
    abstract val isContinueGameEnabled: Boolean
    abstract val isRecyclerScrollEnabled: Boolean

    abstract suspend fun refresh()
}
