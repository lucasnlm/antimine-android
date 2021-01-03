package dev.lucasnlm.external

import com.google.android.gms.tasks.Tasks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeatureFlagManager : IFeatureFlagManager() {
    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().apply {
            setDefaultsAsync(
                mapOf(
                    HISTORY_ENABLED to false,
                    RATE_US_ENABLED to true,
                    IN_APP_ADS_ENABLED to false,
                    GAMEPLAY_EVENTS_ENABLED to false,
                )
            )
        }
    }

    override val isGameHistoryEnabled: Boolean by lazy {
        remoteConfig.getBoolean(HISTORY_ENABLED)
    }

    override val isRateUsEnabled: Boolean by lazy {
        remoteConfig.getBoolean(RATE_US_ENABLED)
    }

    override val isInAppAdsEnabled: Boolean by lazy {
        remoteConfig.getBoolean(IN_APP_ADS_ENABLED)
    }

    override val isGameplayAnalyticsEnabled: Boolean by lazy {
        remoteConfig.getBoolean(GAMEPLAY_EVENTS_ENABLED)
    }

    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            Tasks.await(remoteConfig.fetchAndActivate())
        }
    }

    companion object {
        private const val HISTORY_ENABLED = "history_enabled"
        private const val RATE_US_ENABLED = "rate_us_enabled"
        private const val IN_APP_ADS_ENABLED = "in_app_ads_enabled"
        private const val GAMEPLAY_EVENTS_ENABLED = "gameplay_events_enabled"
    }
}
