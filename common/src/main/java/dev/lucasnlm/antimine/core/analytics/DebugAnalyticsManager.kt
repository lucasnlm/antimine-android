package dev.lucasnlm.antimine.core.analytics

import android.content.Context
import android.util.Log
import dev.lucasnlm.antimine.core.analytics.models.Analytics

class DebugAnalyticsManager : IAnalyticsManager {
    override fun setup(context: Context, properties: Map<String, String>) {
        if (properties.isNotEmpty()) {
            Log.d(TAG, "Setup Analytics using $properties")
        }
    }

    override fun sentEvent(event: Analytics) {
        if (event.extra.isNotEmpty()) {
            Log.d(TAG, "Sent event: '${event.name}' with ${event.extra}")
        } else {
            Log.d(TAG, "Sent event: '${event.name}'")
        }
    }

    companion object {
        val TAG = DebugAnalyticsManager::class.simpleName!!
    }
}
