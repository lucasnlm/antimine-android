package dev.lucasnlm.external

import android.content.Context
import android.os.Bundle
import com.amplitude.api.Amplitude
import com.google.firebase.analytics.FirebaseAnalytics
import org.json.JSONObject

class ExternalAnalyticsWrapper(
    private val context: Context,
) : IExternalAnalyticsWrapper {
    private val firebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }
    private val amplitudeClient by lazy {
        Amplitude
            .getInstance()
            .enableCoppaControl()
            .initialize(context, "AMPLITUDE_API_KEY");
    }

    override fun setup(context: Context, properties: Map<String, String>) {
        properties.forEach { (key, value) ->
            firebaseAnalytics.setUserProperty(key, value)
        }

        amplitudeClient.setUserProperties(JSONObject(properties))
    }

    override fun sendEvent(name: String, content: Map<String, String>) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, name)
            content.forEach { (key, value) ->
                this.putString(key, value)
            }
        }

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        amplitudeClient.logEvent(name, JSONObject(content))
    }
}
