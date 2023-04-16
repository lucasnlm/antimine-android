package dev.lucasnlm.external

abstract class FeatureFlagManager {
    abstract val isGameHistoryEnabled: Boolean
    abstract val isRateUsEnabled: Boolean
    abstract val isInAppAdsEnabled: Boolean
    abstract val isGameplayAnalyticsEnabled: Boolean
    abstract val isGameOverAdEnabled: Boolean
    abstract val isAdsOnContinueEnabled: Boolean
    abstract val isAdsOnNewGameEnabled: Boolean
    abstract val useInterstitialAd: Boolean
    abstract val isContinueGameEnabled: Boolean
    abstract val isRecyclerScrollEnabled: Boolean
    abstract val isFoss: Boolean
    abstract val isThemeTastingEnabled: Boolean
    abstract val minUsageToReview: Int
    abstract val isBannerAdEnabled: Boolean
    abstract val isWeekDaySalesEnabled: Boolean
    abstract val isHexBannerEnabled: Boolean
    abstract val showAdWhenUsingTip: Boolean
    abstract val showCountdownToContinue: Boolean

    abstract suspend fun refresh()
}
