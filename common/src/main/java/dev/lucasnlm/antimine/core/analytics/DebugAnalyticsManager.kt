package dev.lucasnlm.antimine.core.analytics

import android.content.Context
import android.util.Log
import dev.lucasnlm.antimine.core.analytics.models.Analytics

class DebugAnalyticsManager : AnalyticsManager {
    override fun setup(context: Context, userProperties: Map<String, String>) {
        Log.d(TAG, "Setup Analytics using $userProperties")
    }

    override fun sentEvent(event: Analytics) {
        Log.d(TAG, "Sent event: '${event.title}' with ${event.extra}")
    }

    companion object {
        val TAG = DebugAnalyticsManager::class.simpleName
    }
}
