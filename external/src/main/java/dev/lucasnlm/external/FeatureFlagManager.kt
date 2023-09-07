package dev.lucasnlm.external

abstract class FeatureFlagManager {
    abstract val isGameHistoryEnabled: Boolean
    abstract val isRateUsEnabled: Boolean
    abstract val isGameplayAnalyticsEnabled: Boolean
    abstract val isGameOverAdEnabled: Boolean
    abstract val isAdsOnContinueEnabled: Boolean
    abstract val isAdsOnNewGameEnabled: Boolean
    abstract val useInterstitialAd: Boolean
    abstract val isContinueGameEnabled: Boolean
    abstract val isFoss: Boolean
    abstract val minUsageToReview: Int
    abstract val isBannerAdEnabled: Boolean
    abstract val showCountdownToContinue: Boolean

    abstract suspend fun refresh()
}
