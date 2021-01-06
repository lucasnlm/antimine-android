package dev.lucasnlm.external

import com.google.android.gms.tasks.Tasks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeatureFlagManager : IFeatureFlagManager() {
    private val defaultMap = mapOf(
        HISTORY_ENABLED to false,
        RATE_US_ENABLED to true,
        IN_APP_ADS_ENABLED to false,
        GAMEPLAY_EVENTS_ENABLED to false,
        GAME_OVER_AD_ENABLED to true,
        SHOW_ADS_ON_CONTINUE_ENABLED to true,
        CONTINUE_ENABLED to true,
        RECYCLER_SCROLL_ENABLED to true,
    )

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().apply {
            setDefaultsAsync(defaultMap)
        }
    }

    private fun getBoolean(key: String): Boolean {
        return if (BuildConfig.DEBUG) {
            defaultMap[key] as Boolean
        } else {
            remoteConfig.getBoolean(HISTORY_ENABLED)
        }
    }

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

    override val isContinueGameEnabled: Boolean by lazy {
        getBoolean(CONTINUE_ENABLED)
    }

    override val isRecyclerScrollEnabled: Boolean by lazy {
        getBoolean(RECYCLER_SCROLL_ENABLED)
    }

    override suspend fun refresh() {
        if (!BuildConfig.DEBUG) {
            withContext(Dispatchers.IO) {
                Tasks.await(remoteConfig.fetchAndActivate())
            }
        }
    }

    companion object {
        private const val HISTORY_ENABLED = "history_enabled"
        private const val RATE_US_ENABLED = "rate_us_enabled"
        private const val IN_APP_ADS_ENABLED = "in_app_ads_enabled"
        private const val GAMEPLAY_EVENTS_ENABLED = "gameplay_events_enabled"
        private const val GAME_OVER_AD_ENABLED = "game_over_ad_enabled"
        private const val SHOW_ADS_ON_CONTINUE_ENABLED = "ad_on_continue_enabled"
        private const val CONTINUE_ENABLED = "continue_enabled"
        private const val RECYCLER_SCROLL_ENABLED = "recycler_scroll_enabled"
    }
}
