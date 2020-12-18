package dev.lucasnlm.external

interface IFeatureFlagManager {
    fun isGameHistoryEnabled(): Boolean
    fun isRateUsEnabled(): Boolean
    fun isInAppAdsEnabled(): Boolean
}
