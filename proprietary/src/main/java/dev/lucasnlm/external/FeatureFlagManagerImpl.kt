package dev.lucasnlm.external

class FeatureFlagManagerImpl : FeatureFlagManager {
    override val isFoss: Boolean = false

    override val isGameHistoryEnabled: Boolean = false

    override val useInterstitialAd: Boolean = true

    override val isBannerAdEnabled: Boolean = true

    override val showCountdownToContinue: Boolean = false
}
