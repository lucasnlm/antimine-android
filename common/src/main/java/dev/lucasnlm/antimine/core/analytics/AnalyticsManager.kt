package dev.lucasnlm.antimine.core.analytics

import android.content.Context

interface AnalyticsManager {
    fun setup(context: Context, userProperties: Map<String, String>)
    fun sentEvent(event: Event)
}
