package dev.lucasnlm.antimine.core.analytics

import android.app.Application
import android.content.Context
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.core.analytics.models.Analytics
import org.json.JSONObject

class AmplitudeAnalyticsManager(
    private val application: Application
) : AnalyticsManager {

    private var amplitudeClient: AmplitudeClient? = null

    override fun setup(context: Context, userProperties: Map<String, String>) {
        val key = context.getString(R.string.amplitude_key)
        amplitudeClient = Amplitude.getInstance().initialize(application, key).apply {
            setUserProperties(JSONObject(userProperties))
            enableForegroundTracking(application)
        }
    }

    override fun sentEvent(event: Analytics) {
        amplitudeClient?.logEvent(event.title, JSONObject(event.extra))
    }
}
