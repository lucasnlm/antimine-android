package dev.lucasnlm.external

abstract class IFeatureFlagManager {
    abstract val isGameHistoryEnabled: Boolean
    abstract val isRateUsEnabled: Boolean
    abstract val isInAppAdsEnabled: Boolean
    abstract val isGameplayAnalyticsEnabled: Boolean

    abstract suspend fun refresh()
}
