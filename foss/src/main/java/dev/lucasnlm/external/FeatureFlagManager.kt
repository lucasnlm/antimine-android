package dev.lucasnlm.external

class FeatureFlagManager : IFeatureFlagManager {
    override fun isGameHistoryEnabled(): Boolean = true
    override fun isRateUsEnabled(): Boolean = false
}
