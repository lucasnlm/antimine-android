package dev.lucasnlm.external

class FeatureFlagManager : IFeatureFlagManager {
    override fun isGameHistoryEnabled(): Boolean = false
    override fun isRateUsEnabled(): Boolean = true
    override fun isInAppAdsEnabled(): Boolean = false
}
