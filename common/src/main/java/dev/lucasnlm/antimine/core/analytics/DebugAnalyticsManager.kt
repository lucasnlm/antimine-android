package dev.lucasnlm.antimine.core.analytics

import android.content.Context
import android.util.Log
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.external.AnalyticsManager

class DebugAnalyticsManager : AnalyticsManager {
    override fun setup(
        context: Context,
        properties: Map<String, String>,
    ) {
        if (properties.isNotEmpty()) {
            Log.i(TAG, "Setup Analytics using $properties")
        }
    }

    override fun sentEvent(event: Analytics) {
        val message =
            if (event.extra.isNotEmpty()) {
                "Sent event: '${event.name}' with ${event.extra}"
            } else {
                "Sent event: '${event.name}'"
            }

        Log.i(TAG, message)
    }

    companion object {
        val TAG = DebugAnalyticsManager::class.simpleName!!
    }
}
