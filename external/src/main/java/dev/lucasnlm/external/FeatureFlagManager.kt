package dev.lucasnlm.external

interface FeatureFlagManager {
    /**
     * Whether the game history feature is enabled.
     */
    val isGameHistoryEnabled: Boolean

    /**
     * Whether the interstitial ad should be used.
     */
    val useInterstitialAd: Boolean

    /**
     * Whether the app is the FOSS version.
     */
    val isFoss: Boolean

    /**
     * Whether the banner ad should be used.
     */
    val isBannerAdEnabled: Boolean

    /**
     * Whether the countdown to continue should be shown.
     */
    val showCountdownToContinue: Boolean
}
