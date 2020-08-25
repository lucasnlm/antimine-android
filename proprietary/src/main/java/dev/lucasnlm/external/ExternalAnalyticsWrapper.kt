package dev.lucasnlm.external

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class ExternalAnalyticsWrapper(
    private val context: Context,
) : IExternalAnalyticsWrapper {
    private val firebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }

    override fun setup(context: Context, properties: Map<String, String>) {
        properties.forEach { (key, value) ->
            firebaseAnalytics.setUserProperty(key, value)
        }
    }

    override fun sendEvent(name: String, content: Map<String, String>) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, name)
            content.forEach { (key, value) ->
                this.putString(key, value)
            }
        }

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }
}
