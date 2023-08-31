package dev.lucasnlm.external

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dev.lucasnlm.antimine.proprietary.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FeatureFlagManagerImpl : FeatureFlagManager() {
    private val defaultMap = mapOf(
        HISTORY_ENABLED to false,
        RATE_US_ENABLED to true,
        IN_APP_ADS_ENABLED to false,
        GAMEPLAY_EVENTS_ENABLED to false,
        GAME_OVER_AD_ENABLED to true,
        SHOW_ADS_ON_CONTINUE_ENABLED to true,
        SHOW_ADS_ON_NEW_GAME_ENABLED to true,
        CONTINUE_ENABLED to true,
        RECYCLER_SCROLL_ENABLED to true,
        THEME_TASTING_ENABLED to true,
        MIN_USAGE_TO_REVIEW to 5,
        USE_INTERSTITIAL_AD to true,
        BANNER_AD_ENABLED to true,
        ENABLE_WEEK_DAY_SALES to true,
        HEX_BANNER to false,
        SHOW_ADS_WHEN_USE_HINT to true,
        SHOW_COUNTDOWN_TO_CONTINUE to true,
    )

    private var remoteConfig: FirebaseRemoteConfig? = null

    private fun getRemoteConfig(): FirebaseRemoteConfig? {
        return if (remoteConfig != null) {
            remoteConfig
        } else {
            try {
                FirebaseRemoteConfig.getInstance().apply {
                    setDefaultsAsync(defaultMap)
                    remoteConfig = this
                }
            } catch (e: Exception) {
                // Firebase app not initilized yet
                null
            }
        }
    }

    private fun getBoolean(key: String): Boolean {
        val remoteConfig = getRemoteConfig()
        return if (BuildConfig.DEBUG || remoteConfig == null) {
            defaultMap[key] as Boolean
        } else {
            remoteConfig.getBoolean(key)
        }
    }

    private fun getInt(key: String): Int {
        val remoteConfig = getRemoteConfig()
        return if (BuildConfig.DEBUG || remoteConfig == null) {
            defaultMap[key] as Int
        } else {
            remoteConfig.getLong(key).toInt()
        }
    }

    override val isFoss: Boolean = false

    override val isGameHistoryEnabled: Boolean by lazy {
        getBoolean(HISTORY_ENABLED)
    }

    override val isRateUsEnabled: Boolean by lazy {
        getBoolean(RATE_US_ENABLED)
    }

    override val isInAppAdsEnabled: Boolean by lazy {
        getBoolean(IN_APP_ADS_ENABLED)
    }

    override val isGameplayAnalyticsEnabled: Boolean by lazy {
        getBoolean(GAMEPLAY_EVENTS_ENABLED)
    }

    override val isGameOverAdEnabled: Boolean by lazy {
        getBoolean(GAME_OVER_AD_ENABLED)
    }

    override val isAdsOnContinueEnabled: Boolean by lazy {
        getBoolean(SHOW_ADS_ON_CONTINUE_ENABLED)
    }

    override val isAdsOnNewGameEnabled: Boolean by lazy {
        getBoolean(SHOW_ADS_ON_NEW_GAME_ENABLED)
    }

    override val isContinueGameEnabled: Boolean by lazy {
        getBoolean(CONTINUE_ENABLED)
    }

    override val isRecyclerScrollEnabled: Boolean by lazy {
        getBoolean(RECYCLER_SCROLL_ENABLED)
    }

    override val isThemeTastingEnabled: Boolean by lazy {
        getBoolean(THEME_TASTING_ENABLED)
    }

    override val minUsageToReview: Int by lazy {
        getInt(MIN_USAGE_TO_REVIEW)
    }

    override val useInterstitialAd: Boolean by lazy {
        getBoolean(USE_INTERSTITIAL_AD)
    }

    override val isBannerAdEnabled: Boolean by lazy {
        getBoolean(BANNER_AD_ENABLED)
    }

    override val isWeekDaySalesEnabled: Boolean by lazy {
        getBoolean(ENABLE_WEEK_DAY_SALES)
    }

    override val isHexBannerEnabled: Boolean by lazy {
        getBoolean(HEX_BANNER)
    }

    override val showAdWhenUsingTip: Boolean by lazy {
        getBoolean(SHOW_ADS_WHEN_USE_HINT)
    }

    override val showCountdownToContinue: Boolean by lazy {
        getBoolean(SHOW_COUNTDOWN_TO_CONTINUE)
    }

    override suspend fun refresh() {
        val remoteConfig = getRemoteConfig()
        if (!BuildConfig.DEBUG && remoteConfig != null) {
            withContext(Dispatchers.IO) {
                runCatching {
                    remoteConfig.fetchAndActivate().await()
                }.onFailure {
                    Log.e(TAG, "Fail to fetch flags", it)
                }
            }
        }
    }

    companion object {
        private val TAG = FeatureFlagManagerImpl::class.simpleName

        private const val HISTORY_ENABLED = "history_enabled"
        private const val RATE_US_ENABLED = "rate_us_enabled"
        private const val IN_APP_ADS_ENABLED = "in_app_ads_enabled"
        private const val GAMEPLAY_EVENTS_ENABLED = "gameplay_events_enabled"
        private const val GAME_OVER_AD_ENABLED = "game_over_ad_enabled"
        private const val SHOW_ADS_ON_CONTINUE_ENABLED = "ad_on_continue_enabled"
        private const val SHOW_ADS_ON_NEW_GAME_ENABLED = "ad_on_new_game_enabled"
        private const val CONTINUE_ENABLED = "continue_enabled"
        private const val RECYCLER_SCROLL_ENABLED = "recycler_scroll_enabled"
        private const val THEME_TASTING_ENABLED = "theme_tasting_enabled"
        private const val MIN_USAGE_TO_REVIEW = "min_usage_to_review"
        private const val USE_INTERSTITIAL_AD = "use_interstitial_ad"
        private const val BANNER_AD_ENABLED = "banner_ad_enabled"
        private const val ENABLE_WEEK_DAY_SALES = "enable_sales"
        private const val HEX_BANNER = "hex_banner_enabled"
        private const val SHOW_ADS_WHEN_USE_HINT = "show_ads_when_use_tip"
        private const val SHOW_COUNTDOWN_TO_CONTINUE = "show_countdown_to_continue"
    }
}
