package dev.lucasnlm.external

import android.content.Context
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import org.json.JSONObject

class ExternalAnalyticsWrapperImpl(
    private val context: Context,
) : ExternalAnalyticsWrapper {
    private val amplitudeClient: AmplitudeClient? by lazy {
        runCatching {
            Amplitude
                .getInstance()
                .enableCoppaControl()
                .initialize(context, "AMPLITUDE_API_KEY")
        }.getOrNull()
    }

    override fun setup(
        context: Context,
        properties: Map<String, String>,
    ) {
        amplitudeClient?.setUserProperties(JSONObject(properties))
    }

    override fun sendEvent(
        name: String,
        content: Map<String, String>,
    ) {
        amplitudeClient?.logEvent(name, JSONObject(content))
    }
}
