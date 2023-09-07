package dev.lucasnlm.external

class FeatureFlagManagerImpl : FeatureFlagManager() {
    override val isGameHistoryEnabled: Boolean = true
    override val isRateUsEnabled: Boolean = false
    override val isGameplayAnalyticsEnabled: Boolean = false
    override val isGameOverAdEnabled: Boolean = false
    override val isAdsOnNewGameEnabled: Boolean = false
    override val isAdsOnContinueEnabled: Boolean = false
    override val isContinueGameEnabled: Boolean = true
    override val isFoss: Boolean = true
    override val minUsageToReview: Int = Int.MAX_VALUE
    override val useInterstitialAd: Boolean = false
    override val isBannerAdEnabled: Boolean = false
    override val showCountdownToContinue: Boolean = false

    override suspend fun refresh() {
        // No Feature Flags on FOSS
    }
}
