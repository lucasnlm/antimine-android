package dev.lucasnlm.external

class FeatureFlagManagerImpl : FeatureFlagManager {
    override val isGameHistoryEnabled: Boolean = true
    override val isFoss: Boolean = true
    override val useInterstitialAd: Boolean = false
    override val isBannerAdEnabled: Boolean = false
    override val showCountdownToContinue: Boolean = false
}
