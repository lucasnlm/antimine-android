package dev.lucasnlm.antimine.core.analytics

import android.content.Context
import android.util.Log

class DebugAnalyticsManager : AnalyticsManager {
    override fun setup(context: Context, userProperties: Map<String, String>) {
        Log.d(TAG, "Setup Analytics using $userProperties")
    }

    override fun sentEvent(event: Event) {
        Log.d(TAG, "Sent event: '${event.title}' with ${event.extra}")
    }

    companion object {
        const val TAG = "DebugAnalyticsManager"
    }
}
